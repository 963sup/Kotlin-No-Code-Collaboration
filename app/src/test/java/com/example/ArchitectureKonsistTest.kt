package com.example

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.*
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class ArchitectureKonsistTest {
    @Test
    fun `view models stay in the ui viewmodel boundary`() {
        Konsist.scopeFromProduction()
            .classes()
            .withNameEndingWith("ViewModel")
            .assertTrue { it.resideInPackage("..ui.viewmodel..") }
    }

    @Test
    fun `screen files do not bypass persistence boundary`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("..ui.screens..")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.example.data.local") ||
                        import.name.startsWith("com.example.data.repository")
                }
            }
    }

    @Test
    fun `ui component files do not bypass persistence boundary`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("..ui.components..")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("com.example.data.local") ||
                        import.name.startsWith("com.example.data.repository")
                }
            }
    }
}
