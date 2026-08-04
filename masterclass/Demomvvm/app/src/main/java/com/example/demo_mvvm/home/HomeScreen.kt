package com.example.demo_mvvm.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo_mvvm.R
import com.example.demo_mvvm.ui.theme.Bg
import com.example.demo_mvvm.ui.theme.CardLavender
import com.example.demo_mvvm.ui.theme.CardMint
import com.example.demo_mvvm.ui.theme.CardPeach
import com.example.demo_mvvm.ui.theme.DemomvvmTheme
import com.example.demo_mvvm.ui.theme.IconLavender
import com.example.demo_mvvm.ui.theme.IconMint
import com.example.demo_mvvm.ui.theme.IconPeach
import com.example.demo_mvvm.ui.theme.TextPrimary
import com.example.demo_mvvm.ui.theme.TextSecondary

/** Stateful entry point: owns the ViewModel, forwards state and events to [HomeScreen]. */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onTransactionClicked = viewModel::onTransactionClicked,
        onRetry = viewModel::loadData,
    )
}

/** Stateless screen: pure function of [uiState] — previews and tests without a ViewModel. */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTransactionClicked: (String) -> Unit,
    onRetry: () -> Unit,
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            HomeBottomBar(selectedIndex = selectedNavIndex, onSelected = { selectedNavIndex = it })
        },
    ) { innerPadding ->
        when (uiState) {
            HomeUiState.Loading -> LoadingState(Modifier.padding(innerPadding))
            is HomeUiState.Error -> ErrorState(uiState.message, onRetry, Modifier.padding(innerPadding))
            is HomeUiState.Success -> HomeContent(
                data = uiState.data,
                onTransactionClicked = onTransactionClicked,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = IconMint)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = IconMint, contentColor = Color.White),
        ) {
            Text(stringResource(R.string.label_retry))
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeUiData,
    onTransactionClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        HeaderSection(userName = data.userName)
        CardsRow(data = data)
        TransactionsSection(transactions = data.transactions, onTransactionClicked = onTransactionClicked)
    }
}

@Composable
private fun HeaderSection(userName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(50))
                .background(CardPeach),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = stringResource(R.string.cd_avatar),
                tint = IconPeach,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.greeting_hello, userName),
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.greeting_welcome),
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = stringResource(R.string.cd_menu),
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun CardsRow(data: HomeUiData) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActivityCard(
            values = data.activityValues,
            labels = data.activityLabels,
            modifier = Modifier.weight(3f),
        )
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                icon = Icons.Rounded.Sell,
                iconColor = IconPeach,
                backgroundColor = CardPeach,
                label = stringResource(R.string.label_sales),
                value = data.salesLastWeek,
            )
            StatCard(
                icon = Icons.Rounded.PieChart,
                iconColor = IconLavender,
                backgroundColor = CardLavender,
                label = stringResource(R.string.label_revenue),
                value = data.revenueLastWeek,
            )
        }
    }
}

@Composable
private fun ActivityCard(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardMint)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEachIndexed { index, value ->
                val barHeight = (value.toFloat() / maxValue * 64f).coerceAtLeast(12f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(barHeight.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(IconMint),
                    )
                    Text(
                        text = labels.getOrElse(index) { "" },
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }

        Column {
            Text(
                text = stringResource(R.string.label_activity),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.label_activity_subtitle),
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    label: String,
    value: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.padding(top = 10.dp)) {
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun TransactionsSection(
    transactions: List<Transaction>,
    onTransactionClicked: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_transactions),
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.label_see_all),
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            transactions.forEach { transaction ->
                TransactionRow(
                    transaction = transaction,
                    onClick = { onTransactionClicked(transaction.id) },
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: Transaction, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(transaction.rowColor)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50))
                .background(transaction.badgeColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = transaction.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Text(text = transaction.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = transaction.category,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(text = transaction.amount, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                text = transaction.time,
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

private data class NavItem(val icon: ImageVector, val contentDescriptionRes: Int)

@Composable
private fun HomeBottomBar(selectedIndex: Int, onSelected: (Int) -> Unit) {
    val items = listOf(
        NavItem(Icons.Rounded.Home, R.string.cd_nav_home),
        NavItem(Icons.Rounded.AccountBalanceWallet, R.string.cd_nav_wallet),
        NavItem(Icons.Rounded.BarChart, R.string.cd_nav_stats),
        NavItem(Icons.Rounded.Settings, R.string.cd_nav_settings),
    )

    NavigationBar(containerColor = Color.White) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onSelected(index) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = stringResource(item.contentDescriptionRes))
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IconMint,
                    unselectedIconColor = TextSecondary,
                    indicatorColor = IconMint.copy(alpha = 0.18f),
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val previewData = HomeUiData(
        userName = "John Smith",
        activityValues = listOf(20, 40, 80, 40, 20),
        activityLabels = listOf("20", "40", "80", "40", "20"),
        salesLastWeek = "$280.99",
        revenueLastWeek = "$280.99",
        transactions = listOf(
            Transaction(
                id = "tx-1",
                title = "Nike Store",
                category = "Ropa y calzado",
                amount = "-27.67",
                time = "2:23 p.m.",
                icon = Icons.Rounded.Sell,
                badgeColor = Color(0xFF1A1A1A),
                rowColor = CardPeach,
            ),
        ),
    )
    DemomvvmTheme {
        HomeScreen(
            uiState = HomeUiState.Success(previewData),
            onTransactionClicked = {},
            onRetry = {},
        )
    }
}
