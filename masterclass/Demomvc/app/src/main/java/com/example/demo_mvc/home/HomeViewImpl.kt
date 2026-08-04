package com.example.demo_mvc.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo_mvc.databinding.ActivityHomeBinding
import kotlin.math.roundToInt

/**
 * MVC View implementation: inflates the layout, renders widgets, and forwards
 * user interaction to the Controller through [HomeView.Listener]. Contains no
 * business logic — everything here is purely about the UI.
 */
class HomeViewImpl(
    inflater: LayoutInflater,
    container: ViewGroup?,
) : HomeView {

    private val binding = ActivityHomeBinding.inflate(inflater, container, false)
    private var listener: HomeView.Listener? = null
    private val adapter = TransactionAdapter { transaction ->
        listener?.onTransactionClicked(transaction.id)
    }

    private val barViews by lazy {
        listOf(binding.barValue1, binding.barValue2, binding.barValue3, binding.barValue4, binding.barValue5)
    }
    private val barLabels by lazy {
        listOf(binding.barLabel1, binding.barLabel2, binding.barLabel3, binding.barLabel4, binding.barLabel5)
    }

    init {
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(binding.root.context)
        binding.recyclerTransactions.adapter = adapter
        binding.btnRetry.setOnClickListener { listener?.onRetryClicked() }
    }

    override fun getRootView(): View = binding.root

    override fun setListener(listener: HomeView.Listener) {
        this.listener = listener
    }

    override fun showLoading() {
        binding.loadingOverlay.isVisible = true
        binding.errorOverlay.isVisible = false
    }

    override fun showData(data: HomeUiData) {
        binding.loadingOverlay.isVisible = false
        binding.errorOverlay.isVisible = false

        binding.tvHello.text = binding.root.context.getString(
            com.example.demo_mvc.R.string.greeting_hello,
            data.userName,
        )
        binding.tvSalesValue.text = data.salesLastWeek
        binding.tvRevenueValue.text = data.revenueLastWeek

        renderActivityChart(data.activityValues, data.activityLabels)

        adapter.submitList(data.transactions)
    }

    override fun showError(message: String) {
        binding.loadingOverlay.isVisible = false
        binding.errorOverlay.isVisible = true
        binding.tvErrorMessage.text = message
    }

    private fun renderActivityChart(values: List<Int>, labels: List<String>) {
        val density = binding.root.context.resources.displayMetrics.density
        val maxBarHeightDp = 64f
        val maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1)

        values.forEachIndexed { index, value ->
            if (index >= barViews.size) return@forEachIndexed
            val heightDp = (value.toFloat() / maxValue * maxBarHeightDp).coerceAtLeast(12f)
            barViews[index].layoutParams = barViews[index].layoutParams.apply {
                height = (heightDp * density).roundToInt()
            }
            barViews[index].requestLayout()
        }
        labels.forEachIndexed { index, label ->
            if (index >= barLabels.size) return@forEachIndexed
            barLabels[index].text = label
        }
    }
}
