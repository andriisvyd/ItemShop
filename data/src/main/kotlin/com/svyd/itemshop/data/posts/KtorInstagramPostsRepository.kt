package com.svyd.itemshop.data.posts

import com.svyd.itemshop.data.common.runCatchingDomain
import com.svyd.itemshop.data.posts.mapper.toDomainOrNull
import com.svyd.itemshop.data.posts.remote.InstagramGraphApi
import com.svyd.itemshop.domain.auth.TokenStorage
import com.svyd.itemshop.domain.common.DomainError
import com.svyd.itemshop.domain.common.DomainResult
import com.svyd.itemshop.domain.posts.InstagramPost
import com.svyd.itemshop.domain.posts.InstagramPostsRepository

internal class KtorInstagramPostsRepository(
    private val api: InstagramGraphApi,
    private val tokenStorage: TokenStorage,
) : InstagramPostsRepository {

    override suspend fun loadPosts(): DomainResult<List<InstagramPost>> {
        val token = tokenStorage.read()?.accessToken?.raw
            ?: return DomainResult.Failure(DomainError.Unauthorized())
        return runCatchingDomain {
            api.fetchMyMedia(accessToken = token).data.mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun getPost(id: String): DomainResult<InstagramPost?> {
        val token = tokenStorage.read()?.accessToken?.raw
            ?: return DomainResult.Failure(DomainError.Unauthorized())
        return runCatchingDomain {
            api.fetchMedia(id = id, accessToken = token).toDomainOrNull()
        }
    }
}
