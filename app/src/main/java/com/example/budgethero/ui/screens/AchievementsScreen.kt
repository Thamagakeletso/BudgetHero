package com.example.budgethero.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budgethero.data.achievements.Achievement
import com.example.budgethero.data.achievements.AchievementManager
import com.example.budgethero.ui.theme.*
import com.example.budgethero.ui.viewmodels.ExpenseViewModel
import com.example.budgethero.ui.viewmodels.CategoryViewModel

/**
 * AchievementsScreen displays all badges earned by the user.
 * Part of the gamification system to encourage consistent budgeting.
 * Locked achievements are shown as greyed out to motivate users.
 */
@Composable
fun AchievementsScreen(
    expenseViewModel: ExpenseViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val achievementManager = remember { AchievementManager(context) }

    val expenses by expenseViewModel.expenses.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    // Check achievements based on current data
    LaunchedEffect(expenses, categories) {
        Log.d("AchievementsScreen", "Checking achievements")
        achievementManager.checkExpenseAchievements(expenses.size)
        achievementManager.checkCategoryAchievements(categories.size)
    }

    val achievements = achievementManager.getAchievements()
    val unlockedCount = achievements.count { it.isUnlocked }
    val totalCount = achievements.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        // ── Top Bar ───────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(16.dp)
        ) {
            Text(
                "Achievements",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Progress Summary Card ─────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BrandGreen
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🏆",
                            fontSize = 48.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$unlockedCount / $totalCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Achievements Unlocked",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                unlockedCount.toFloat() / totalCount.toFloat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            // ── Achievement header ────────────────────────
            item {
                Text(
                    "Your Badges",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            }

            // ── Achievement Cards ─────────────────────────
            items(achievements, key = { it.id }) { achievement ->
                AchievementCard(achievement = achievement)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

/**
 * Single achievement card showing badge emoji, title and description.
 * Locked achievements are displayed in grey to show what's possible.
 * @param achievement The achievement data to display
 */
@Composable
fun AchievementCard(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                SurfaceWhite
            else
                Color(0xFFF0F0F0)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge emoji with background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = if (achievement.isUnlocked)
                            BrandGreenLight
                        else
                            Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (achievement.isUnlocked)
                        achievement.emoji
                    else
                        "🔒",
                    fontSize = 24.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (achievement.isUnlocked)
                        TextPrimary
                    else
                        TextSecondary
                )
                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Unlocked indicator
            if (achievement.isUnlocked) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = BrandGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}