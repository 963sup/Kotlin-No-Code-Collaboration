package com.example.data.repository

import com.example.data.local.GovernanceDao
import com.example.data.model.Enterprise
import com.example.data.model.OwnerType
import com.example.data.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GovernanceRepositoryMockKTest {

    private lateinit var dao: GovernanceDao
    private lateinit var repository: GovernanceRepository

    private val testUser = User(
        id = "usr-test-1",
        enterpriseId = "ent-test-1",
        username = "alex.dev",
        displayName = "Alex Dev",
        email = "alex@test.internal",
        title = "Senior Architect",
        isEnterpriseAdmin = true,
        canOwnerRepository = true,
    )

    private val testEnterprise = Enterprise(
        id = "ent-test-1",
        name = "Test Enterprise",
        slug = "test-ent",
        description = "Enterprise Unit Test",
    )

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        every { dao.getAllEnterprises() } returns flowOf(listOf(testEnterprise))
        every { dao.getEnterprise() } returns flowOf(testEnterprise)
        every { dao.getAllOrganizations() } returns flowOf(emptyList())
        every { dao.getAllUsers() } returns flowOf(listOf(testUser))
        every { dao.getAllTeams() } returns flowOf(emptyList())
        every { dao.getAllRepositories() } returns flowOf(emptyList())
        every { dao.getAllAuditLogs() } returns flowOf(emptyList())
        every { dao.getAllRepoAccessRules() } returns flowOf(emptyList())
        every { dao.getAllOrgMemberships() } returns flowOf(emptyList())
        every { dao.getAllTeamMemberships() } returns flowOf(emptyList())
        every { dao.getAllArtifacts() } returns flowOf(emptyList())
        every { dao.getAllNotifications() } returns flowOf(emptyList())
        every { dao.getAllIssues() } returns flowOf(emptyList())
        every { dao.getAllDiscussions() } returns flowOf(emptyList())
        every { dao.getAllReviews() } returns flowOf(emptyList())
        every { dao.getAllApprovals() } returns flowOf(emptyList())
        every { dao.getAllDependencies() } returns flowOf(emptyList())

        repository = GovernanceRepository(dao)
    }

    @Test
    fun updateEnterpriseDelegatesToDaoWithMockK() = runTest {
        val updatedEnterprise = testEnterprise.copy(name = "Updated Enterprise Name")
        coEvery { dao.updateEnterprise(any()) } returns Unit

        repository.updateEnterprise(updatedEnterprise)

        coVerify(exactly = 1) { dao.updateEnterprise(updatedEnterprise) }
    }

    @Test
    fun createRepositoryWithValidUserOwnerSucceedsAndCallsDao() = runTest {
        val (success, message) = repository.createRepository(
            name = "analytics-hub",
            displayName = "Analytics Hub",
            ownerType = OwnerType.USER,
            ownerId = testUser.id,
            ownerDisplayName = testUser.displayName,
            enterpriseId = testEnterprise.id,
            description = "User personal repo",
            category = "Analytics",
            creatorUser = testUser,
        )

        assertTrue(success)
        assertEquals("Repository created successfully!", message)

        coVerify(exactly = 1) { dao.insertRepository(match { it.name == "analytics-hub" && it.ownerId == testUser.id }) }
        coVerify(exactly = 1) { dao.insertRepoAccessRule(any()) }
        coVerify(exactly = 1) { dao.insertAuditLog(any()) }
    }

    @Test
    fun createRepositoryWithOrgOwnerSucceedsAndCallsDao() = runTest {
        val (success, message) = repository.createRepository(
            name = "platform-services",
            displayName = "Platform Services",
            ownerType = OwnerType.ORGANIZATION,
            ownerId = "org-1",
            ownerDisplayName = "Platform Org",
            enterpriseId = testEnterprise.id,
            description = "Org shared repo",
            category = "Infrastructure",
            creatorUser = testUser,
        )

        assertTrue(success)
        assertEquals("Repository created successfully!", message)

        coVerify(exactly = 1) {
            dao.insertRepository(
                match {
                    it.name == "platform-services" &&
                        it.ownerType == OwnerType.ORGANIZATION &&
                        it.requiredApproverCount == 2
                },
            )
        }
        coVerify(exactly = 1) { dao.insertRepoAccessRule(any()) }
        coVerify(exactly = 1) { dao.insertAuditLog(any()) }
    }
}
