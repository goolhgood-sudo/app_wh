package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.TaskCategory
import com.example.data.model.TaskPriority
import com.example.nlp.ArabicNlpParser
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
    assertEquals("تطبيق مهام المكي", appName)
  }

  @Test
  fun `test arabic nlp date and priority extraction`() {
    val input = "اجتماع عمل غداً الساعة 5 مساءً عاجل"
    val result = ArabicNlpParser.parse(input)
    assertEquals(TaskPriority.HIGH, result.priority)
    assertEquals(TaskCategory.WORK, result.category)
    assertNotNull(result.dueDate)
    assertNotNull(result.matchedDateSummary)
  }
}
