package com.example.app.android.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.app.android.R
import com.example.app.android.components.profile.ConnectedAccountRow
import com.example.app.android.components.profile.PreferenceToggleRow
import com.example.app.android.components.profile.ProfileDivider
import com.example.app.android.components.profile.ProfileHeader
import com.example.app.android.components.profile.ProfileInfoRow
import com.example.app.android.components.profile.ProfileSection
import com.example.app.android.model.UserProfile
import com.example.app.android.theme.AppTheme

private data class LanguageOption(
    val tag: String,
    val displayName: String
)

private val languages = listOf(
    LanguageOption("pt-BR", "Português"),
    LanguageOption("en", "English"),
    LanguageOption("es", "Español")
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {
    val colors = AppTheme.colors
    val scrollState = rememberScrollState()

    val user = remember {
        UserProfile(
            name = "Leonardo Oliveira",
            email = "leo.oliveira@email.com",
            phone = "+55 11 99876-5432",
            address = "Rua das Flores, 123",
            location = "Osasco, SP",
            initials = "LO",
            preferences = emptyList()
        )
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var googleConnected by remember { mutableStateOf(true) }
    var appleConnected by remember { mutableStateOf(false) }
    var facebookConnected by remember { mutableStateOf(false) }

    // Photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) photoUri = uri }

    // Language state
    val currentLocale = AppCompatDelegate.getApplicationLocales()
    val currentTag = if (currentLocale.isEmpty) "" else currentLocale.toLanguageTags()
    var selectedTag by remember {
        mutableStateOf(
            languages.firstOrNull { currentTag.startsWith(it.tag, ignoreCase = true) }?.tag
                ?: languages.first().tag
        )
    }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // --- Profile Header ---
        ProfileHeader(
            name = user.name,
            email = user.email,
            initials = user.initials,
            photoUri = photoUri,
            onAvatarClick = { photoPickerLauncher.launch("image/*") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Personal Info Section ---
        ProfileSection(title = stringResource(R.string.profile_section_personal)) {
            ProfileInfoRow(
                icon = Icons.Outlined.Email,
                label = stringResource(R.string.profile_email),
                value = user.email
            )
            ProfileDivider()
            ProfileInfoRow(
                icon = Icons.Outlined.Phone,
                label = stringResource(R.string.profile_phone),
                value = user.phone
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Location Section ---
        ProfileSection(title = stringResource(R.string.profile_section_location)) {
            ProfileInfoRow(
                icon = Icons.Outlined.Home,
                label = stringResource(R.string.profile_address),
                value = user.address
            )
            ProfileDivider()
            ProfileInfoRow(
                icon = Icons.Outlined.LocationOn,
                label = stringResource(R.string.profile_location),
                value = user.location
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Preferences Section ---
        ProfileSection(title = stringResource(R.string.profile_section_preferences)) {
            PreferenceToggleRow(
                label = stringResource(R.string.pref_notifications),
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Language Section (dropdown select) ---
        ProfileSection(title = stringResource(R.string.language_label)) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { languageMenuExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = languages.first { it.tag == selectedTag }.displayName,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lang.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTag == lang.tag) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedTag == lang.tag) colors.accent else colors.textPrimary
                                )
                            },
                            onClick = {
                                selectedTag = lang.tag
                                languageMenuExpanded = false
                                val localeList = LocaleListCompat.forLanguageTags(lang.tag)
                                AppCompatDelegate.setApplicationLocales(localeList)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Connected Accounts Section ---
        ProfileSection(title = stringResource(R.string.profile_section_connected)) {
            ConnectedAccountRow(
                icon = Icons.Outlined.Email,
                name = stringResource(R.string.connected_google),
                connected = googleConnected,
                statusLabel = stringResource(R.string.connected_status),
                connectLabel = stringResource(R.string.connected_connect),
                onToggle = { googleConnected = !googleConnected }
            )
            ProfileDivider()
            ConnectedAccountRow(
                icon = Icons.Outlined.Phone,
                name = stringResource(R.string.connected_apple),
                connected = appleConnected,
                statusLabel = stringResource(R.string.connected_status),
                connectLabel = stringResource(R.string.connected_connect),
                onToggle = { appleConnected = !appleConnected }
            )
            ProfileDivider()
            ConnectedAccountRow(
                icon = Icons.Outlined.Person,
                name = stringResource(R.string.connected_facebook),
                connected = facebookConnected,
                statusLabel = stringResource(R.string.connected_status),
                connectLabel = stringResource(R.string.connected_connect),
                onToggle = { facebookConnected = !facebookConnected }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Logout Button ---
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.destructive,
                contentColor = colors.onDestructive
            )
        ) {
            Text(
                text = stringResource(R.string.profile_logout),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
