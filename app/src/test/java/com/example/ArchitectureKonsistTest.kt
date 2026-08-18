package com.example

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.classes
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureKonsistTest {
    @Test
    fun `view models stay in the ui viewmodel boundary`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..ui.viewmodel") }
    }

    @Test
    fun `screen classes stay outside persistence packages`() {
        Konsist.scopeFromProject()
            .classes()
            .withNameEndingWith("Screen")
            .assertTrue {
                it.resideOutsidePackage("..data.local") &&
                    it.resideOutsidePackage("..data.repository")
            }
    }
}
