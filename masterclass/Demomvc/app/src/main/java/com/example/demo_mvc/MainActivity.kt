package com.example.demo_mvc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demo_mvc.home.HomeModel
import com.example.demo_mvc.home.HomeView
import com.example.demo_mvc.home.HomeViewImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * MVC Controller for the Home screen (see references/ui-pattern-mvc.md in the
 * android-development skill). The Activity implements the View's Listener and
 * mediates directly between View and Model — there is no Presenter layer in MVC.
 *
 * This demo instantiates Model/View manually (no Hilt) to keep the MVC pattern
 * itself front and center; a production app would inject them instead.
 */
class MainActivity : AppCompatActivity(), HomeView.Listener {

    private val model = HomeModel()
    private lateinit var view: HomeView
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = HomeViewImpl(layoutInflater, null)
        setContentView(view.getRootView())
        view.setListener(this)

        loadHomeData()
    }

    override fun onDestroy() {
        controllerScope.cancel()
        super.onDestroy()
    }

    private fun loadHomeData() {
        view.showLoading()
        controllerScope.launch {
            try {
                val data = model.fetchHomeData()
                view.showData(data)
            } catch (e: Exception) {
                view.showError(e.message ?: getString(R.string.error_generic))
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        controllerScope.launch { model.markTransactionSeen(transactionId) }
    }

    override fun onRetryClicked() = loadHomeData()
}
