package edu.ucsb.cs.cs184.kstubbsgeopics.register

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import edu.ucsb.cs.cs184.kstubbsgeopics.BR
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import edu.ucsb.cs.cs184.kstubbsgeopics.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel
    var email = ""
    var password = ""
    var username = ""
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var emailBox: EditText
    private lateinit var passwordBox: EditText
    private lateinit var registerButton: Button
    lateinit var binding: FragmentRegisterBinding
    private lateinit var usersRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.setLifecycleOwner(this)
        registerButton = binding.buttonRegister

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

        return binding.root
        //return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        database = FirebaseDatabase.getInstance()
        usersRef = database.getReference("Users")

        viewModel.email.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            email = viewModel.email.value.toString()
        })
        viewModel.password.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            password = viewModel.password.value.toString()
        })
        viewModel.username.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            username = viewModel.username.value.toString()
        })

        binding.setVariable(BR.viewModel, viewModel)

        registerButton.setOnClickListener {
            if (username.length == 0 || email.length == 0 || password.length == 0) {
                Toast.makeText(context, "Please fill in all the boxes.",
                        Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener() { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                val user = auth.currentUser
                                if (user != null) {
                                    auth.updateCurrentUser(user)
                                }
                                if (user != null) {
                                    usersRef.child(user.uid).child("UID").setValue(user.uid)
                                    usersRef.child(user.uid).child("Username").setValue(username)
                                    usersRef.child(user.uid).child("Email").setValue(email)
                                }
                                findNavController().navigate(R.id.action_RegisterFragment_to_MapsFragment)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(context, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()
                                //updateUI(null)
                            }
                        }
            }
        }
    }
}