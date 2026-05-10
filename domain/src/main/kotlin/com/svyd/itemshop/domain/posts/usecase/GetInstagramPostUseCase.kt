package com.svyd.itemshop.domain.posts.usecase

import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.InstagramPost
import com.svyd.itemshop.domain.posts.InstagramPostsRepository

class GetInstagramPostUseCase(
    private val repository: InstagramPostsRepository,
) {
    suspend operator fun invoke(id: String): DomainResult<InstagramPost?> = repository.getPost(id)
}
