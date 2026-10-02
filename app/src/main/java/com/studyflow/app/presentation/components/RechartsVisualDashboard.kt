package com.studyflow.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.presentation.viewmodel.DailyStudyPoint
import kotlin.math.max

/**
 * Visual Dashboard Component inspired by Recharts data visualization library.
 * Designed with native Jetpack Compose Canvas, animated radial gauges, and Cartesian bar charts.
 */
@Composable
fun RechartsVisualDashboard(
    timeStudiedTodayMinutes: Int,
    timeStudiedWeekMinutes: Int,
    timeStudiedMonthMinutes: Int,
    timeStudiedTotalMinutes: Int,
    dailyGoalMinutes: Int,
    dailyGoalProgressPercent: Float,
    tasksCompletedToday: Int,
    tasksTotalToday: Int,
    last7DaysHistory: List<DailyStudyPoint>,
    modifier: Modifier = Modifier
) {
    var selectedTimePeriod by remember { mutableStateOf(0) } // 0: Hoje, 1: Semana, 2: Mês, 3: Total
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_visual_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card: Total Study Time with Recharts Aesthetic
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
                                    )
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "RECHARTS METRICS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Period Selector Chips (Hoje, Semana, Mês, Total)
                val periods = listOf("Hoje", "Semana", "Mês", "Total")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    periods.forEachIndexed { index, period ->
                        val isSelected = selectedTimePeriod == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { selectedTimePeriod = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted Metric
                val displayMinutes = when (selectedTimePeriod) {
                    0 -> timeStudiedTodayMinutes
                    1 -> timeStudiedWeekMinutes
                    2 -> timeStudiedMonthMinutes
                    else -> timeStudiedTotalMinutes
                }
                val hours = displayMinutes / 60
                val mins = displayMinutes % 60

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tempo Total de Estudo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${hours}h ${mins}m",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${displayMinutes} min)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Recharts Radial Gauge: Progresso das Metas Diárias
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Progresso da Meta Diária",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Meta diária de ${dailyGoalMinutes} min planejados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Radial Gauge
                    val animatedProgress by animateFloatAsState(
                        targetValue = (dailyGoalProgressPercent / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 1000),
                        label = "dailyGoalRadialProgress"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            val strokeWidth = 14.dp.toPx()

                            // Background Track
                            drawArc(
                                color = Color(0xFFE2E8F0),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Foreground Gradient Arc
                            val gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6))
                            )
                            drawArc(
                                brush = gradient,
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${dailyGoalProgressPercent.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "concluído",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Progress Details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (dailyGoalProgressPercent >= 100f) Color(0xFFECFDF5) else Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (dailyGoalProgressPercent >= 100f) Color(0xFF059669) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (dailyGoalProgressPercent >= 100f) "Meta Batida! 🎉" else if (dailyGoalProgressPercent >= 50f) "Mais da metade!" else "Em andamento",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dailyGoalProgressPercent >= 100f) Color(0xFF059669) else Color(0xFF2563EB)
                                )
                            }
                        }

                        Text(
                            text = "⏱️ $timeStudiedTodayMinutes / $dailyGoalMinutes min hoje",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "📋 $tasksCompletedToday de $tasksTotalToday tarefas do dia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Recharts-style Cartesian Bar Chart: Histórico dos Últimos 7 Dias
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Distribuição de Estudo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Últimos 7 dias (toque em uma barra para detalhes)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Legend
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                        Text(
                            text = "Minutos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Recharts Tooltip inspection
                selectedBarIndex?.let { index ->
                    if (index in last7DaysHistory.indices) {
                        val selectedPoint = last7DaysHistory[index]
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedPoint.dayLabel} ${if (selectedPoint.isToday) "(Hoje)" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${selectedPoint.minutes} min (${selectedPoint.minutes / 60}h ${selectedPoint.minutes % 60}m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF67E8F9),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Bar Chart Canvas
                val maxMinutes = max(60, last7DaysHistory.maxOfOrNull { it.minutes } ?: 60)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(last7DaysHistory) {
                                detectTapGestures { offset ->
                                    val count = last7DaysHistory.size
                                    if (count > 0) {
                                        val totalWidth = size.width
                                        val barSlotWidth = totalWidth / count
                                        val tappedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, count - 1)
                                        selectedBarIndex = if (selectedBarIndex == tappedIndex) null else tappedIndex
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx() // Reserve space for X-axis labels
                        val count = last7DaysHistory.size
                        if (count == 0) return@Canvas

                        val barWidth = 26.dp.toPx()
                        val stepX = canvasWidth / count

                        // 1. Recharts Cartesian Grid Lines (3 horizontal dotted lines)
                        val gridLinePathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1f)
                        gridLevels.forEach { level ->
                            val y = canvasHeight * (1f - level)
                            drawLine(
                                color = Color(0xFFE2E8F0),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = gridLinePathEffect
                            )
                        }

                        // Baseline
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, canvasHeight),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // 2. Bars
                        last7DaysHistory.forEachIndexed { index, point ->
                            val barHeightFraction = (point.minutes.toFloat() / maxMinutes).coerceIn(0f, 1f)
                            val barHeight = (canvasHeight * barHeightFraction).coerceAtLeast(6.dp.toPx())
                            val centerX = (index * stepX) + (stepX / 2f)
                            val left = centerX - (barWidth / 2f)
                            val top = canvasHeight - barHeight

                            val isSelected = selectedBarIndex == index
                            val barBrush = if (point.isToday) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                )
                            } else if (isSelected) {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                                )
                            }

                            // Rounded top bar
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    // X-axis day labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last7DaysHistory.forEachIndexed { index, point ->
                            val isSelected = selectedBarIndex == index
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBarIndex = if (selectedBarIndex == index) null else index
                                    }
                            ) {
                                Text(
                                    text = point.dayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (point.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (point.isToday) Color(0xFF8B5CF6) else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (point.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8B5CF6))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
