-- ============================================================
--  Manara Admin Panel — Supabase setup (run once in SQL editor)
-- ============================================================

-- 1) Admin flag on profiles -----------------------------------
alter table public.profiles add column if not exists is_admin boolean default false;
-- mark yourself admin (replace with your auth user id / email):
-- update public.profiles set is_admin = true where email = 'YOUR_ADMIN_EMAIL';

-- helper: is the current user an admin?
create or replace function public.is_admin()
returns boolean language sql stable security definer as $$
  select coalesce((select is_admin from public.profiles where id = auth.uid()), false);
$$;

-- 2) "last seen" for active-users metric ----------------------
alter table public.profiles add column if not exists last_seen timestamptz;
-- the app can call this RPC on launch to mark the user active:
create or replace function public.touch_last_seen()
returns void language sql security definer as $$
  update public.profiles set last_seen = now() where id = auth.uid();
$$;

-- 3) Admin write access on content tables ---------------------
-- subjects
drop policy if exists "admin_write_subjects" on public.subjects;
create policy "admin_write_subjects" on public.subjects
  for all using (public.is_admin()) with check (public.is_admin());
-- files
drop policy if exists "admin_write_files" on public.files;
create policy "admin_write_files" on public.files
  for all using (public.is_admin()) with check (public.is_admin());

-- admin can read every profile (for the users screen)
drop policy if exists "admin_read_profiles" on public.profiles;
create policy "admin_read_profiles" on public.profiles
  for select using (public.is_admin() or auth.uid() = id);

-- 4) In-app notifications / announcements ---------------------
create table if not exists public.notifications (
  id uuid default gen_random_uuid() primary key,
  title text not null,
  body text not null,
  created_at timestamptz default now()
);
alter table public.notifications enable row level security;
drop policy if exists "read_notifications" on public.notifications;
create policy "read_notifications" on public.notifications
  for select using (auth.role() = 'authenticated');
drop policy if exists "admin_write_notifications" on public.notifications;
create policy "admin_write_notifications" on public.notifications
  for all using (public.is_admin()) with check (public.is_admin());

-- 4b) saved_files: each user manages ONLY their own rows -------
alter table public.saved_files enable row level security;
drop policy if exists "saved_select_own" on public.saved_files;
create policy "saved_select_own" on public.saved_files
  for select using (auth.uid() = user_id);
drop policy if exists "saved_insert_own" on public.saved_files;
create policy "saved_insert_own" on public.saved_files
  for insert with check (auth.uid() = user_id);
-- DELETE policy is REQUIRED for the app's "unsave" to work under RLS:
drop policy if exists "saved_delete_own" on public.saved_files;
create policy "saved_delete_own" on public.saved_files
  for delete using (auth.uid() = user_id);

-- 5) Storage bucket "pdfs" policies ---------------------------
-- create the bucket named "pdfs" (Private) from the dashboard first, then:
insert into storage.buckets (id, name, public) values ('pdfs','pdfs', false)
  on conflict (id) do nothing;
-- admins can upload / manage objects
drop policy if exists "admin_manage_pdfs" on storage.objects;
create policy "admin_manage_pdfs" on storage.objects
  for all using (bucket_id = 'pdfs' and public.is_admin())
  with check (bucket_id = 'pdfs' and public.is_admin());
-- authenticated users can read (the app uses signed URLs)
drop policy if exists "read_pdfs" on storage.objects;
create policy "read_pdfs" on storage.objects
  for select using (bucket_id = 'pdfs' and auth.role() = 'authenticated');
