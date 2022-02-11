package com.ruta.perubus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.ruta.perubus.api.Api
import com.ruta.perubus.api.RetrofitClient
import com.ruta.perubus.models.LoggedInUser
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.Exception

enum class ProviderType {
    Facebook
}

class CredentialActivity : AppCompatActivity() {

    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var user: LoggedInUser
    private lateinit var service: Api
    private lateinit var retrofit: Retrofit
    private var celular: String = ""
    private var pass: String = ""
//    val requestCall = apiService.userLogin(NroCelular, Contrasenia)

    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credential)
        service = createApiService()
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        val loginUser: Button = findViewById(R.id.LoginUser)
        val username: EditText = findViewById(R.id.username)
        val password: EditText = findViewById(R.id.password)
        val facebook: Button = findViewById(R.id.login_button)

        loginUser.setOnClickListener {
            celular = username.text.toString().trim()
            pass = password.text.toString().trim()

            if (celular.isNotEmpty()) {
                if (pass.isNotEmpty()) {
                    executeLogin(celular, pass)
                }else{
                    Toast.makeText(this@CredentialActivity, "Contraseña vacía", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MapsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }else{
                Toast.makeText(this@CredentialActivity, "Email vacio", Toast.LENGTH_SHORT).show()
            }

        }

        facebook.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

                LoginManager.getInstance().registerCallback(callbackManager,
                object  : FacebookCallback<LoginResult>{

                    override fun onSuccess(result: LoginResult?) {
                        result?.let {
                            val token = it.accessToken

                            val credential = FacebookAuthProvider.getCredential(token.token)

                            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                                if (it.isSuccessful){
                                    showHome(it.result?.user?.email ?: "", ProviderType.Facebook)
                                } else{
                                    showAlert()
                                }
                            }
                        }
                    }
                    override fun onCancel() {

                    }
                    override fun onError(error: FacebookException?) {
                            showAlert()
                    }
                })
        }

    }

    private fun executeLogin(celular: String, pass: String){
        val call = service.userLogin("userLogin", celular, pass )
            call.enqueue(object : Callback<String>{
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful){
                        try {
                            val jsonUser = JSONObject(response.body()!!)
                            val jsonId = jsonUser.optString("NroCelular")
                            val jsonPassword = jsonUser.optString("Contrasenia")
                            user = LoggedInUser(jsonId, jsonPassword)

                            Toast.makeText(this@CredentialActivity, "Login Correcto", Toast.LENGTH_SHORT).show()
                        }catch (e: Exception){
                            Log.d("login", e.toString())
                            Toast.makeText(this@CredentialActivity, "Login incorrecto", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("login", t.toString())
                }
            })

    }

    private fun createApiService(): Api{
        retrofit = Retrofit.Builder()
            .baseUrl("http://181.224.255.236:1001/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        return retrofit.create(Api::class.java)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, MapsActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }
}
