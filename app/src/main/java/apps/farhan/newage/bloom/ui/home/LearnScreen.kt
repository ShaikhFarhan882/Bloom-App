package apps.farhan.newage.bloom.ui.home
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apps.farhan.newage.bloom.ui.components.*

data class FaqItem(val question: String, val answer: String)
data class FaqSection(val title: String, val items: List<FaqItem>)

object LearnContent {
    // The design only provides questions. Replace placeholders with approved, reviewed copy.
    private const val PLACEHOLDER = "Explainer content goes here. Replace with approved copy."

    private fun q(question: String, answer: String = PLACEHOLDER) = FaqItem(question, answer)

    val sections = listOf(
        FaqSection("Diagnosis & basics", listOf(
            q("What exactly is PCOS?",
                "PCOS stands for polycystic ovary syndrome, a common hormonal condition. It can affect periods, ovulation, skin, hair and metabolism, and it looks different from person to person."),
            q("What does PCOS stand for, and do I actually have cysts on my ovaries?",
                "It stands for polycystic ovary syndrome. The \"cysts\" are usually small follicles, and you can have PCOS without them."),
            q("What causes PCOS?",
                "The exact cause isn't fully understood. Genetics, higher androgen levels and insulin resistance are all thought to play a part."),
            q("How is PCOS actually diagnosed?"),
            q("Is PCOS the same for everyone?"),
        )),
        FaqSection("Symptoms & body", listOf(
            q("Why are my periods irregular?"),
            q("Why does weight feel harder to manage?"),
            q("What is the deal with insulin resistance?"),
            q("Is the acne and hair growth from PCOS treatable?"),
        )),
        FaqSection("Managing it day to day", listOf(
            q("Can food really make a difference?"),
            q("What foods should I eat, and which should I limit?"),
            q("How can I manage insulin resistance day to day?"),
            q("Will I need medication forever?"),
            q("How does stress fit into all this?"),
        )),
        FaqSection("Looking ahead", listOf(
            q("Does PCOS affect fertility?"),
            q("What happens if PCOS is left unmanaged?"),
        )),
    )
}

@Composable
fun LearnScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScreenHeader("Learn", "Understand PCOS, at your own pace")
        LearnContent.sections.forEach { section ->
            SectionTitle(section.title)
            section.items.forEach { FaqAccordion(it) }
        }
    }
}

@Composable
private fun FaqAccordion(item: FaqItem) {
    var expanded by rememberSaveable(item.question) { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, label = "chevron")

    BloomCard(onClick = { expanded = !expanded }) {
        Column(Modifier.padding(14.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.question, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.rotate(rotation)) { ChevronIcon() }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(item.answer, fontSize = 13.sp, lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}