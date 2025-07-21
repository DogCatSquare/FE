package com.example.dogcatsquare.ui.community

import com.example.dogcatsquare.data.model.community.Comment

interface CommentActionListener {
    fun onReplyClicked(comment: com.example.dogcatsquare.data.model.community.Comment)
    fun onDeleteClicked(comment: com.example.dogcatsquare.data.model.community.Comment)
}
