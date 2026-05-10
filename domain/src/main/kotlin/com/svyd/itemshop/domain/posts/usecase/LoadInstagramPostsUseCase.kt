package com.svyd.itemshop.domain.posts.usecase

import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.InstagramPost
import com.svyd.itemshop.domain.posts.InstagramPostsRepository

class LoadInstagramPostsUseCase(
    private val repository: InstagramPostsRepository,
) {
    suspend operator fun invoke(): DomainResult<List<InstagramPost>> = repository.loadPosts()
}
