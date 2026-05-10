package com.svyd.itemshop.domain.posts

import com.svyd.itemshop.domain.common.DomainResult

interface InstagramPostsRepository {
    /**
     * Fetch the first page of the authenticated user's media. v1 returns
     * the first ~25 items; pagination is a follow-up.
     */
    suspend fun loadPosts(): DomainResult<List<InstagramPost>>

    /** Lookup by id; reads from the same fresh-fetch source. */
    suspend fun getPost(id: String): DomainResult<InstagramPost?>
}
