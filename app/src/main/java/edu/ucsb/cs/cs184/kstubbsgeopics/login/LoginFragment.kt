package edu.ucsb.cs.cs184.kstubbsgeopics.login

import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleObserver
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import edu.ucsb.cs.cs184.kstubbsgeopics.BR
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import java.util.*
import edu.ucsb.cs.cs184.kstubbsgeopics.databinding.FragmentLoginBinding
import edu.ucsb.cs.cs184.kstubbsgeopics.BR.viewModel

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    var email = ""
    var password = ""
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var loginButton: Button
    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.setLifecycleOwner(this)
        loginButton = binding.buttonLogin

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        viewModel.email.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            email = viewModel.email.value.toString()
        })
        viewModel.password.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            password = viewModel.password.value.toString()
        })

        binding.setVariable(BR.viewModel, viewModel)

        loginButton.setOnClickListener{
            if (email.length == 0 || password.length == 0) {
                Toast.makeText(context, "Please fill all of the boxes.",
                        Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                if (user != null) {
                                    auth.updateCurrentUser(user)
                                }
                                findNavController().navigate(R.id.action_LoginFragment_to_MapsFragment)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(context, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
            }
        }
    }
}