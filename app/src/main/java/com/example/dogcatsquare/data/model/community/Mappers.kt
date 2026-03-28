package com.example.dogcatsquare.data.model.community

fun PostListItem.toResult(): GetAllPostResult =
    GetAllPostResult(
        id = id,
        board = boardType,
        username = username,
        animal_type = animalType ?: "",
        title = title,
        content = content,
        videoURL = videoUrl,
        thumbnailURL = thumbnailUrl?.takeIf { it.isNotBlank() },
        profileImageURL = profileImageUrl,
        images = images.orEmpty(),
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt
    )