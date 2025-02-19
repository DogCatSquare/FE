package com.example.dogcatsquare.ui.community

import com.example.dogcatsquare.data.community.Comment

interface CommentActionListener {
    fun onReplyClicked(comment: Comment)
    fun onDeleteClicked(comment: Comment)
}
