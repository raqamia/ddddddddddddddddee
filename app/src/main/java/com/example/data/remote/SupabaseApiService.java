package com.example.data.remote;

import com.example.data.remote.dto.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseApiService {
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/v1/signup")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/v1/token?grant_type=refresh_token")
    Call<AuthResponse> refreshToken(@Body Map<String, String> body);

    @POST("auth/v1/logout")
    Call<Void> logout(@Header("Authorization") String bearer);

    @GET("rest/v1/profiles")
    Call<List<ProfileDto>> getProfile(@Query("id") String eqUserId, @Header("Authorization") String bearer);

    @PATCH("rest/v1/profiles")
    Call<Void> updateProfile(@Query("id") String eqUserId, @Body Map<String, Object> updates, @Header("Authorization") String bearer);

    @GET("rest/v1/subjects")
    Call<List<SubjectDto>> getSubjects(@Query("track") String trackFilter, @Query("is_active") String isActive, @Header("Authorization") String bearer);

    @GET("rest/v1/files")
    Call<List<FileDto>> getFiles(@Query("subject_id") String subjectIdEq, @Query("category") String categoryEq, @Header("Authorization") String bearer);

    @GET("rest/v1/saved_files")
    Call<List<SavedFileDto>> getSavedFiles(@Query("user_id") String userIdEq, @Query("select") String select, @Header("Authorization") String bearer);

    @POST("rest/v1/saved_files")
    Call<Void> saveFile(@Body Map<String, String> body, @Header("Authorization") String bearer);

    @DELETE("rest/v1/saved_files")
    Call<Void> unsaveFile(@Query("user_id") String userIdEq, @Query("file_id") String fileIdEq, @Header("Authorization") String bearer);

    @POST("storage/v1/object/sign/{bucket}/{path}")
    Call<SignedUrlResponse> getSignedUrl(@Path("bucket") String bucket, @Path(value = "path", encoded = true) String path, @Body Map<String, Integer> expiresIn, @Header("Authorization") String bearer);
}
