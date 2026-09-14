package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.repository.UnicodeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Charbon", appName)
  }

  @Test
  fun `verify U25AD White Rectangle character exists in UnicodeRepository`() {
    val charU25AD = UnicodeRepository.getCharacter(0x25AD)
    assertNotNull(charU25AD)
    assertEquals("25AD", charU25AD.hex)
    assertEquals("WHITE RECTANGLE", charU25AD.name)
    assertEquals("Geometric Shapes", charU25AD.blockName)
  }

  @Test
  fun `verify Unicode blocks coverage`() {
    val blocks = UnicodeRepository.BLOCKS
    assert(blocks.isNotEmpty())
    val geo = blocks.find { it.id == "GEOMETRIC" }
    assertNotNull(geo)
    val chars = UnicodeRepository.getCharactersForBlock(geo!!)
    assert(chars.any { it.codePoint == 0x25AD })
  }
}

