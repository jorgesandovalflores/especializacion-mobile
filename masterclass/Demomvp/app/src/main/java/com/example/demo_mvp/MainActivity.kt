package com.example.demo_mvp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demo_mvp.databinding.ActivityHomeBinding
import com.example.demo_mvp.home.HomeContract
import com.example.demo_mvp.home.HomeModel
import com.example.demo_mvp.home.HomePresenter
import com.example.demo_mvp.home.HomeUiData
import com.example.demo_mvp.home.TransactionAdapter
import kotlin.math.roundToInt

/**
 * MVP View for the Home screen (see references/ui-pattern-mvp.md in the
 * android-development skill). The Activity IS the View: it implements
 * [HomeContract.View] directly, does all UI/widget work, and holds zero
 * business logic — every user action and result flows through the Presenter.
 *
 * This demo instantiates Model/Presenter manually (no Hilt) to keep the MVP
 * pattern itself front and center; a production app would inject them instead.
 */
class MainActivity : AppCompatActivity(), HomeContract.View {

    private lateinit var binding: ActivityHomeBinding
    private val presenter: HomeContract.Presenter = HomePresenter(HomeModel())

    private val adapter = TransactionAdapter { transaction ->
        presenter.onTransactionClicked(transaction.id)
    }

    private val barViews by lazy {
        listOf(binding.barValue1, binding.barValue2, binding.barValue3, binding.barValue4, binding.barValue5)
    }
    private val barLabels by lazy {
        listOf(binding.barLabel1, binding.barLabel2, binding.barLabel3, binding.barLabel4, binding.barLabel5)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        binding.recyclerTransactions.adapter = adapter
        binding.btnRetry.setOnClickListener { presenter.loadData() }

        presenter.attachView(this)
        presenter.loadData()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun showLoading() {
        binding.loadingOverlay.isVisible = true
        binding.errorOverlay.isVisible = false
    }

    override fun hideLoading() {
        binding.loadingOverlay.isVisible = false
    }

    override fun showData(data: HomeUiData) {
        binding.errorOverlay.isVisible = false

        binding.tvHello.text = getString(R.string.greeting_hello, data.userName)
        binding.tvSalesValue.text = data.salesLastWeek
        binding.tvRevenueValue.text = data.revenueLastWeek

        renderActivityChart(data.activityValues, data.activityLabels)

        adapter.submitList(data.transactions)
    }

    override fun showError(message: String) {
        binding.errorOverlay.isVisible = true
        binding.tvErrorMessage.text = message
    }

    private fun renderActivityChart(values: List<Int>, labels: List<String>) {
        val density = resources.displayMetrics.density
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
