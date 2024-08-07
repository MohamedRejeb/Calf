package com.mohamedrejeb.calf.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mohamedrejeb.calf.navigation.rememberNavController
import com.mohamedrejeb.calf.sample.navigation.AppNavGraph
import com.mohamedrejeb.calf.sample.screens.DateSelector
import com.mohamedrejeb.calf.sample.ui.theme.CalfTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun App() =
    CalfTheme {
        val navController = rememberNavController()

        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            AppNavGraph(navController = navController)

//            var date by remember {
//                val dateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
//                mutableStateOf(
//                    LocalDate(
//                        year = dateTime.year,
//                        monthNumber = dateTime.monthNumber,
//                        dayOfMonth = dateTime.dayOfMonth - 5
//                    )
//                )
//            }
//
//            DateSelector(
//                initialDate = date,
//                onDateSelected = {
//                    date = it
//                }
//            )
        }
    }
