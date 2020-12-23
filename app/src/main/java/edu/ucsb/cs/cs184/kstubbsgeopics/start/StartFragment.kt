package edu.ucsb.cs.cs184.kstubbsgeopics.start

import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import edu.ucsb.cs.cs184.kstubbsgeopics.R


class StartFragment : Fragment() {

    companion object {
        fun newInstance() = StartFragment()
    }

    private lateinit var viewModel: StartViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_start, container, false)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            findNavController().navigate(R.id.action_StartFragment_to_MapsFragment)
        }
        view.findViewById<Button>(R.id.button_login).setOnClickListener{
            findNavController().navigate(R.id.action_StartFragment_to_LoginFragment)
        }
        view.findViewById<Button>(R.id.button_register).setOnClickListener{
            findNavController().navigate(R.id.action_StartFragment_to_RegisterFragment)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Log.d(ContentValues.TAG, "back pressed")
        }

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StartViewModel::class.java)
    }

}