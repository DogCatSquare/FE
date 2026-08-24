package com.dogcatsquare.dogcatsquare.ui.community

import com.dogcatsquare.dogcatsquare.data.model.community.Comment

interface CommentActionListener {
    fun onReplyClicked(comment: com.dogcatsquare.dogcatsquare.data.model.community.Comment)
    fun onDeleteClicked(comment: com.dogcatsquare.dogcatsquare.data.model.community.Comment)
}
