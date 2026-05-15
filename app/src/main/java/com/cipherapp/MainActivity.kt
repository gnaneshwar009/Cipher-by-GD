package com.cipherapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
//  Colour palette
// ─────────────────────────────────────────────────────────────────────────────
private val BgDeep          = Color(0xFF08080C)
private val BgSurface       = Color(0xFF0F0F18)
private val BgCard          = Color(0xFF13131E)
private val BorderSubtle    = Color(0xFF1E1E30)
private val BorderGlow      = Color(0xFF6C3FEF)
private val AccentPurple    = Color(0xFF7C4DFF)
private val AccentCyan      = Color(0xFF00E5FF)
private val AccentPink      = Color(0xFFE040FB)
private val TextPrimary     = Color(0xFFEEEEFF)
private val TextSecondary   = Color(0xFF7878AA)
private val TextHint        = Color(0xFF3A3A5C)
private val EncryptGradient = listOf(Color(0xFF7C4DFF), Color(0xFFE040FB))
private val DecryptGradient = listOf(Color(0xFF00BCD4), Color(0xFF00E5FF))
private val GlowPurple      = Color(0x447C4DFF)
private val GlowCyan        = Color(0x4400E5FF)

// ─────────────────────────────────────────────────────────────────────────────
//  Activity
// ─────────────────────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CipherAppTheme {
                CipherScreen()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Theme
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CipherAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background     = BgDeep,
            surface        = BgSurface,
            primary        = AccentPurple,
            secondary      = AccentCyan,
            onBackground   = TextPrimary,
            onSurface      = TextPrimary,
            onPrimary      = Color.White
        ),
        typography = Typography(
            bodyLarge  = TextStyle(fontFamily = FontFamily.Monospace, color = TextPrimary),
            bodyMedium = TextStyle(fontFamily = FontFamily.Monospace, color = TextSecondary)
        ),
        content = content
    )
}

// ─────────────────────────────────────────────────────────────────────────────
//  Tab model
// ─────────────────────────────────────────────────────────────────────────────
enum class CipherTab { ENCRYPT, DECRYPT }

// ─────────────────────────────────────────────────────────────────────────────
//  Main screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CipherScreen() {
    var selectedTab by remember { mutableStateOf(CipherTab.ENCRYPT) }

    var encryptInput  by remember { mutableStateOf("") }
    var decryptInput  by remember { mutableStateOf("") }

    val encryptOutput = remember(encryptInput)  { CipherEngine.encrypt(encryptInput) }
    val decryptOutput = remember(decryptInput)  { CipherEngine.decrypt(decryptInput) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        // Ambient glow blobs
        AmbientGlows()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            // ── App header ──────────────────────────────────────────────────
            AppHeader()

            Spacer(Modifier.height(32.dp))

            // ── Tab selector ────────────────────────────────────────────────
            TabSelector(
                selected  = selectedTab,
                onSelect  = { selectedTab = it }
            )

            Spacer(Modifier.height(28.dp))

            // ── Panels ──────────────────────────────────────────────────────
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (slideInHorizontally { if (targetState == CipherTab.ENCRYPT) -it else it } +
                     fadeIn()) togetherWith
                    (slideOutHorizontally { if (targetState == CipherTab.ENCRYPT) it else -it } +
                     fadeOut())
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    CipherTab.ENCRYPT -> EncryptPanel(
                        input   = encryptInput,
                        output  = encryptOutput,
                        onInput = { encryptInput = it },
                        onCopy  = { copyToClipboard(context, encryptOutput, "Encrypted text") }
                    )
                    CipherTab.DECRYPT -> DecryptPanel(
                        input   = decryptInput,
                        output  = decryptOutput,
                        onInput = { decryptInput = it },
                        onCopy  = { copyToClipboard(context, decryptOutput, "Decrypted text") }
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── Footer ──────────────────────────────────────────────────────
            Text(
                text      = "CVV Substitution Cipher  •  95-char keyspace",
                color     = TextHint,
                fontSize  = 11.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Ambient glow decoration
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AmbientGlows() {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowOffset"
    )

    Canvas(Modifier.fillMaxSize()) {
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(GlowPurple, Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * (0.05f + offset * 0.05f)),
                radius = 340f
            ),
            radius = 340f,
            center = Offset(size.width * 0.15f, size.height * (0.05f + offset * 0.05f))
        )
        drawCircle(
            brush  = Brush.radialGradient(
                colors = listOf(GlowCyan, Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * (0.6f - offset * 0.05f)),
                radius = 280f
            ),
            radius = 280f,
            center = Offset(size.width * 0.85f, size.height * (0.6f - offset * 0.05f))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  App header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(AccentPurple, AccentPink),
                        start  = Offset(0f, 0f),
                        end    = Offset(72f, 72f)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.Lock,
                contentDescription = "Cipher",
                tint               = Color.White,
                modifier           = Modifier.size(36.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text       = "CIPHER VAULT",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color      = TextPrimary,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "Substitution cipher • Secure • Offline",
            fontSize   = 12.sp,
            color      = TextSecondary,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Tab selector
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TabSelector(selected: CipherTab, onSelect: (CipherTab) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(4.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            CipherTab.values().forEach { tab ->
                val isSelected = selected == tab
                val gradient   = if (tab == CipherTab.ENCRYPT) EncryptGradient else DecryptGradient
                val label      = if (tab == CipherTab.ENCRYPT) "ENCRYPT" else "DECRYPT"
                val icon       = if (tab == CipherTab.ENCRYPT) Icons.Filled.LockOutline
                                 else Icons.Filled.LockOpen

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (isSelected)
                                Modifier.background(Brush.linearGradient(gradient))
                            else
                                Modifier.background(Color.Transparent)
                        )
                        .clickable { onSelect(tab) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector        = icon,
                            contentDescription = label,
                            tint               = if (isSelected) Color.White else TextSecondary,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = label,
                            color      = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Encrypt panel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun EncryptPanel(
    input:   String,
    output:  String,
    onInput: (String) -> Unit,
    onCopy:  () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {

        SectionLabel(
            label     = "PLAINTEXT INPUT",
            sublabel  = "Type your message to encrypt",
            iconColor = AccentPurple
        )
        Spacer(Modifier.height(10.dp))

        InputField(
            value       = input,
            onValue     = onInput,
            placeholder = "Enter text to encrypt…",
            accentColor = AccentPurple,
            gradientColors = EncryptGradient
        )

        Spacer(Modifier.height(8.dp))
        CharCountRow(input.length, EncryptGradient)

        Spacer(Modifier.height(24.dp))

        // Arrow indicator
        ArrowDivider(label = "ENCRYPTED", gradientColors = EncryptGradient)

        Spacer(Modifier.height(24.dp))

        SectionLabel(
            label     = "CIPHERTEXT OUTPUT",
            sublabel  = "Read-only encrypted result",
            iconColor = AccentPink
        )
        Spacer(Modifier.height(10.dp))

        OutputField(
            value          = output,
            placeholder    = "Encrypted text will appear here…",
            gradientColors = EncryptGradient,
            onCopy         = onCopy
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Decrypt panel
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DecryptPanel(
    input:   String,
    output:  String,
    onInput: (String) -> Unit,
    onCopy:  () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {

        SectionLabel(
            label     = "CIPHERTEXT INPUT",
            sublabel  = "Paste encrypted text to decode",
            iconColor = AccentCyan
        )
        Spacer(Modifier.height(10.dp))

        InputField(
            value          = input,
            onValue        = onInput,
            placeholder    = "Paste ciphertext to decrypt…",
            accentColor    = AccentCyan,
            gradientColors = DecryptGradient
        )

        Spacer(Modifier.height(8.dp))
        CharCountRow(input.length, DecryptGradient)

        Spacer(Modifier.height(24.dp))

        ArrowDivider(label = "DECRYPTED", gradientColors = DecryptGradient)

        Spacer(Modifier.height(24.dp))

        SectionLabel(
            label     = "PLAINTEXT OUTPUT",
            sublabel  = "Read-only decrypted result",
            iconColor = AccentCyan
        )
        Spacer(Modifier.height(10.dp))

        OutputField(
            value          = output,
            placeholder    = "Decrypted text will appear here…",
            gradientColors = DecryptGradient,
            onCopy         = onCopy
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Reusable components
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(label: String, sublabel: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp, 20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(iconColor)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text       = label,
                fontSize   = 11.sp,
                fontWeight = FontWeight.Bold,
                color      = iconColor,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text       = sublabel,
                fontSize   = 11.sp,
                color      = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun CharCountRow(count: Int, gradientColors: List<Color>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = Icons.Outlined.TextFields,
            contentDescription = null,
            tint               = TextHint,
            modifier           = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text       = "$count chars",
            fontSize   = 11.sp,
            color      = TextHint,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ArrowDivider(label: String, gradientColors: List<Color>) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Left line
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(BorderSubtle)
        )
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(gradientColors))
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = label,
                    fontSize   = 10.sp,
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(BorderSubtle)
        )
    }
}

@Composable
fun InputField(
    value:          String,
    onValue:        (String) -> Unit,
    placeholder:    String,
    accentColor:    Color,
    gradientColors: List<Color>
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue    = if (focused) accentColor else BorderSubtle,
        animationSpec  = tween(300),
        label          = "inputBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(
                width  = if (focused) 1.5.dp else 1.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(16.dp)
            )
    ) {
        // Top accent line
        if (focused) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.horizontalGradient(gradientColors))
            )
        }

        BasicOutlinedInput(
            value       = value,
            onValue     = onValue,
            placeholder = placeholder,
            onFocus     = { focused = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasicOutlinedInput(
    value:       String,
    onValue:     (String) -> Unit,
    placeholder: String,
    onFocus:     (Boolean) -> Unit
) {
    TextField(
        value               = value,
        onValueChange       = onValue,
        modifier            = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 140.dp),
        placeholder         = {
            Text(
                text       = placeholder,
                color      = TextHint,
                fontSize   = 14.sp,
                fontFamily = FontFamily.Monospace
            )
        },
        textStyle           = TextStyle(
            color      = TextPrimary,
            fontSize   = 15.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight  = 24.sp
        ),
        colors              = TextFieldDefaults.colors(
            focusedContainerColor        = Color.Transparent,
            unfocusedContainerColor      = Color.Transparent,
            focusedIndicatorColor        = Color.Transparent,
            unfocusedIndicatorColor      = Color.Transparent,
            cursorColor                  = AccentPurple,
            focusedTextColor             = TextPrimary,
            unfocusedTextColor           = TextPrimary
        ),
        keyboardOptions     = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect    = false,
            keyboardType   = KeyboardType.Text,
            imeAction      = ImeAction.Default
        ),
        maxLines            = 10
    )
}

@Composable
fun OutputField(
    value:          String,
    placeholder:    String,
    gradientColors: List<Color>,
    onCopy:         () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(BgCard)
                .border(
                    width = 1.dp,
                    color = BorderSubtle,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        ) {
            // Top gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.horizontalGradient(gradientColors))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 140.dp)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text       = placeholder,
                        color      = TextHint,
                        fontSize   = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    SelectionContainer {
                        Text(
                            text       = value,
                            color      = TextPrimary,
                            fontSize   = 15.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight  = 24.sp
                        )
                    }
                }
            }
        }

        // Copy button bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            gradientColors[0].copy(alpha = 0.18f),
                            gradientColors[1].copy(alpha = 0.18f)
                        )
                    )
                )
                .border(
                    1.dp,
                    BorderSubtle,
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .clickable(enabled = value.isNotEmpty()) { onCopy() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy",
                    tint               = if (value.isNotEmpty()) gradientColors[0] else TextHint,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = if (value.isNotEmpty()) "COPY TO CLIPBOARD" else "NOTHING TO COPY",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color      = if (value.isNotEmpty()) gradientColors[0] else TextHint
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Utility
// ─────────────────────────────────────────────────────────────────────────────
private fun copyToClipboard(context: Context, text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

// Workaround import alias for SelectionContainer
@Composable
private fun SelectionContainer(content: @Composable () -> Unit) {
    androidx.compose.foundation.text.selection.SelectionContainer(content = content)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF08080C, widthDp = 390, heightDp = 844)
@Composable
fun CipherScreenPreview() {
    CipherAppTheme { CipherScreen() }
}
