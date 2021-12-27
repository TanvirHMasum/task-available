package com.example.currencyconverter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.example.service.PreferencesProvider
import com.example.service.SharedPrefData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

/*Developer: Tanvir Masum
Email: tanvirh.masum07@gmail.com
Phone: +8801737887114
Date: 27-Dec-2021*/

class MainActivity : AppCompatActivity() {
    private lateinit var preferenceProvider: PreferencesProvider
    var baseCurrency = "EUR"
    var convertedCurrency = "USD"
    var conversionRate = 0.0
    var commissionFee = 0.7

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferenceProvider = PreferencesProvider(applicationContext)

        val et_EUR: TextView = findViewById(R.id.et_EUR)
        val et_USD: TextView = findViewById(R.id.et_USD)

        if (preferenceProvider.getString(SharedPrefData.EUR_Balance) == null || preferenceProvider.getString(
                SharedPrefData.EUR_Balance
            ) == ""
        ) {
            preferenceProvider.putString(SharedPrefData.EUR_Balance, "5000")
        }
        if (preferenceProvider.getString(SharedPrefData.USD_Balance) == null || preferenceProvider.getString(
                SharedPrefData.USD_Balance
            ) == ""
        ) {
            preferenceProvider.putString(SharedPrefData.USD_Balance, "100")
        }

        et_EUR.setText(preferenceProvider.getString(SharedPrefData.EUR_Balance) + " EUR")
        et_USD.setText(preferenceProvider.getString(SharedPrefData.USD_Balance) + " USD")

        val buttonSubmit: Button = findViewById(R.id.submit_BTN)
        buttonSubmit.setOnClickListener {
            callDialogView()
        }

        spinnerInvoke()
        textChangedHandle()
    }

    private fun spinnerInvoke() {
        val spinner: Spinner = findViewById(R.id.spinner_firstConversion)
        val spinner2: Spinner = findViewById(R.id.spinner_secondConversion)

        ArrayAdapter.createFromResource(
            this,
            R.array.base_currency,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.converted_currency,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
        }

        spinner.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }

        })

        spinner2.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedCurrency = parent?.getItemAtPosition(position).toString()
                getApiResult()
            }
        })
    }

    private fun textChangedHandle() {
        val base_ET: EditText = findViewById(R.id.base_ET)
        base_ET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                try {
                    getApiResult()
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Type a value", Toast.LENGTH_SHORT).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("Main", "Before Text Changed")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("Main", "OnTextChanged")
            }
        })
    }

    private fun getApiResult() {
        val base_ET: EditText = findViewById(R.id.base_ET)
        val converted_ET: EditText = findViewById(R.id.converted_ET)
        if (base_ET.text.isNotEmpty() && base_ET.text.isNotBlank()) {

            val API =
                "http://api.exchangeratesapi.io/v1/latest?access_key=96d63681feac11761fe404cab4e6e061&base=$baseCurrency&symbols=$convertedCurrency"

            if (baseCurrency == convertedCurrency) {
                Toast.makeText(
                    applicationContext, "Pick different currency to convert", Toast.LENGTH_SHORT
                ).show()
            } else {

                GlobalScope.launch(Dispatchers.IO) {

                    try {
                        val apiResult = URL(API).readText()
                        val jsonObject = JSONObject(apiResult)
                        conversionRate =
                            jsonObject.getJSONObject("rates").getString(convertedCurrency)
                                .toDouble()

                        Log.d("GlobalScope", "$conversionRate")
                        Log.d("GlobalScope", apiResult)

                        withContext(Dispatchers.Main) {
                            val text =
                                ((base_ET.text.toString()
                                    .toFloat()) * conversionRate).toString()
                            converted_ET.setText(text)
                        }

                    } catch (e: Exception) {
                        Log.e("GlobalScope", "$e")
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun callDialogView() {
        val base_ET: EditText = findViewById(R.id.base_ET)
        val converted_ET: EditText = findViewById(R.id.converted_ET)
        val et_EUR: TextView = findViewById(R.id.et_EUR)
        val et_USD: TextView = findViewById(R.id.et_USD)

        val base_currency: String = base_ET.text.toString()
        val converted_currency: String = converted_ET.text.toString()

        val alertDialogBuilder = AlertDialog.Builder(this)
        val diaView: View =
            layoutInflater.inflate(R.layout.dialog_convertion_info, null)
        alertDialogBuilder.setView(diaView)

        val dialog_converted_info = diaView.findViewById<TextView>(R.id.dialog_converted_info)

        dialog_converted_info.setText("You have converted $base_currency $baseCurrency to $converted_currency $convertedCurrency. Commission Fee - $commissionFee $baseCurrency")
        val convertDialog = alertDialogBuilder.create()

        if (baseCurrency == "EUR") {
            val existingBalanceEUR: String? =
                preferenceProvider.getString(SharedPrefData.EUR_Balance)
            val restBalanceEUR: Double =
                (existingBalanceEUR!!.toDouble() - (base_currency.toDouble() + commissionFee))
            preferenceProvider.putString(SharedPrefData.EUR_Balance, restBalanceEUR.toString())
            et_EUR.setText(preferenceProvider.getString(SharedPrefData.EUR_Balance) + " EUR")
        }

        if (convertedCurrency == "USD") {
            val existingBalanceUSD: String? =
                preferenceProvider.getString(SharedPrefData.USD_Balance)
            val restBalanceUSD: Double =
                (existingBalanceUSD!!.toDouble() + converted_currency.toDouble())
            preferenceProvider.putString(SharedPrefData.USD_Balance, restBalanceUSD.toString())
            et_USD.setText(preferenceProvider.getString(SharedPrefData.USD_Balance) + " USD")
        }

        convertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        convertDialog.show()
        convertDialog.setCancelable(false)

        val doneDialogBtn = diaView.findViewById<TextView>(R.id.dialog_done_BTN)

        doneDialogBtn.setOnClickListener { v1: View? ->
            convertDialog.dismiss()
        }
    }

}