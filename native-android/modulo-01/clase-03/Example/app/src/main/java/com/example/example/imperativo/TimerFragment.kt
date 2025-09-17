package com.example.example.imperativo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.example.R

class TimerFragment : Fragment(R.layout.fragment_timer) {

    private lateinit var txtCounter: TextView
    private var seconds: Int = 0

    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            seconds++
            txtCounter.text = "Seconds: $seconds"
            handler.postDelayed(this, 1000)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtCounter = view.findViewById(R.id.txtCounter)
        Log.d("TimerFragment", "onViewCreated → View creada")
    }

    override fun onStart() {
        super.onStart()
        handler.post(ticker)
        Log.d("TimerFragment", "onStart → Timer iniciado")
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(ticker)
        Log.d("TimerFragment", "onStop → Timer detenido")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        Log.d("TimerFragment", "onDestroyView → View destruida y callbacks limpiados")
    }
}
