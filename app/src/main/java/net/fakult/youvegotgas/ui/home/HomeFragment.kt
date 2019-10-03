package net.fakult.youvegotgas.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import net.fakult.youvegotgas.NotificationManager
import net.fakult.youvegotgas.R

private const val RC_SIGN_IN = 101

class HomeFragment : Fragment()
{
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        //val noteMan = NotificationManager(context!!, null, null, null, null)
        //noteMan.showNotification(R.layout.notification_leaving_home, "text", "leaving_home", 111)

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        this.requestPermissions(permissions, 0)

        homeViewModel = ViewModelProviders.of(this)
            .get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()

        //auth.signOut();
        //googleSignInClient.signOut()

        val currentUser = auth.currentUser

        Log.d("Currentuser", auth.toString())

        val tutorialComplete = activity?.getPreferences(Context.MODE_PRIVATE)
            ?.getInt("tutorial_complete", 0)

        if (tutorialComplete == 1 && currentUser != null)
        {
            val textView: TextView = root.findViewById(R.id.text_home)
            homeViewModel.text.observe(this, Observer {
                textView.text = "Stats shown here"
            })
        }
        else
        {
            val textView: TextView = root.findViewById(R.id.text_home)
            homeViewModel.text.observe(this, Observer {
                textView.text = it
            })

            /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context!!, gso)

            signIn()*/

            /*
            auth.createUserWithEmailAndPassword("", "")
                .addOnCompleteListener(activity as Activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("FBAUth", "createUserWithEmail:success")
                        val user = auth.currentUser
                        //updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("FBAuth", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(context, "Authentication failed.",
                                       Toast.LENGTH_SHORT).show()
                        //updateUI(null)
                    }

                    // ...
                }
            */

            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())

            // Create and launch sign-in intent
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), RC_SIGN_IN)

            activity?.getPreferences(Context.MODE_PRIVATE)
                ?.edit()
                ?.putInt("tutorial_complete", 1)
                ?.apply()
        }

        return root
    }

    private fun signIn()
    {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1001)
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            }
            catch (e: ApiException)
            {
                // Google Sign In failed, update UI appropriately
                Log.w("GSignin", "Google sign in failed" + e.message)
                // ...
            }
        }
        else if (requestCode == RC_SIGN_IN)
        {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK)
            {
                // Successfully signed in
                val user = FirebaseAuth.getInstance()
                    .currentUser
                // ...
            }
            else
            {
                Log.d("Sign in fire", "Sign in failed")
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount)
    {
        Log.d("firebaseGsignin", "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity as Activity) { task ->
                if (task.isSuccessful)
                {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GoogleAuth", "signInWithCredential:success")
                    val user = auth.currentUser
                    //updateUI(user)
                }
                else
                {
                    // If sign in fails, display a message to the user.
                    Log.w("GoogleAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Authentication Failed.", Toast.LENGTH_SHORT)
                        .show()
                    //updateUI(null)
                }
            }
    }
}