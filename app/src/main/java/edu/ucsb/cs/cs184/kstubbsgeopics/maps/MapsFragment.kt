package edu.ucsb.cs.cs184.kstubbsgeopics.maps

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.ucsb.cs.cs184.kstubbsgeopics.BR
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import edu.ucsb.cs.cs184.kstubbsgeopics.User
import edu.ucsb.cs.cs184.kstubbsgeopics.databinding.FragmentMapsBinding
import edu.ucsb.cs.cs184.kstubbsgeopics.post.Post
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    companion object {
        fun newInstance() = MapsFragment()
    }

    private var mMap: GoogleMap? = null
    val viewModel: MapsViewModel by activityViewModels()
    val IMAGE_CAPTURE_REQUEST = 1
    lateinit var currentPhotoPath: String
    var currentCaption = ""
    lateinit var captionEditText: EditText
    lateinit var instructionsTextView: TextView
    lateinit var captionsTextView: TextView
    lateinit var binding: FragmentMapsBinding
    private lateinit var confirmFAB: FloatingActionButton
    private lateinit var clearFAB: FloatingActionButton
    private lateinit var listFAB: FloatingActionButton
    private lateinit var cameraFAB: FloatingActionButton
    private lateinit var crossHair: ImageView
    private lateinit var database: FirebaseDatabase
    private lateinit var postsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    val PERMISSION_ALL = 1
    val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    )

    var editMode = false

    var posts: ArrayList<Post> = ArrayList()
    var users: ArrayList<User> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false)
        binding.setLifecycleOwner(this)

        setHasOptionsMenu(true)

        confirmFAB = binding.buttonConfirm
        clearFAB = binding.buttonClear
        listFAB = binding.buttonList
        captionEditText = binding.editTextCaption
        instructionsTextView = binding.textViewInstructions
        captionsTextView = binding.textViewCaption
        cameraFAB = binding.buttonPhotoCamera
        crossHair = binding.crosshair

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        }


        auth = FirebaseAuth.getInstance()

        instructionsTextView.setOnClickListener {}

        cameraFAB.setOnClickListener {
            if (!hasPermissions()) {
                requestPermissions(
                    PERMISSIONS,
                    PERMISSION_ALL
                )
            } else {
                takePicture()
            }

        }

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

        val supportMapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var counter = 0
        for (i in grantResults) {
            if (i == 0)
                counter++
        }
        if (counter == grantResults.size)
            takePicture()
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults!!)
    }

    fun hasPermissions(): Boolean {
        for (perm in PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "edu.ucsb.cs.cs184.kstubbsgeopics.maps.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            editView()
            viewModel.editMode.value = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.logout, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            auth.signOut()
            findNavController().navigate(R.id.action_MapsFragment_to_StartFragment)
        }
        return true
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            viewModel.currentPhotoPath.value = absolutePath
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (!viewModel.started.value!!) {
            if (!resources.getBoolean(R.bool.isTablet))
                viewModel.mapZoom.value = 15F
            ActivityCompat.requestPermissions(requireContext() as Activity, PERMISSIONS, PERMISSION_ALL)
        }

        viewModel.currentPhotoPath.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            currentPhotoPath = viewModel.currentPhotoPath.value!!
        })
        viewModel.currentCaption.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            currentCaption = viewModel.currentCaption.value!!
        })
        viewModel.editMode.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            editMode = viewModel.editMode.value!!
        })

        binding.setVariable(BR.viewModel, viewModel)

        database = FirebaseDatabase.getInstance()
        postsRef = database.getReference().child("Posts")
        usersRef = database.getReference().child("Users")

        postsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                posts.clear()
                for (snapshot in snapshot.getChildren()) {
                    var postUID = snapshot.key
                    var userUID = snapshot.child("userUID").value.toString()
                    var photoPath = snapshot.child("photoPath").value.toString()
                    var latitude = snapshot.child("latitude").value as Double
                    var longitude = snapshot.child("longitude").value as Double
                    var caption = snapshot.child("caption").value.toString()

                    var post = Post(postUID!!, userUID, photoPath, latitude, longitude, caption)
                    posts.add(post)
                }
                viewModel.totalPosts.value = posts
                if (!viewModel.started.value!!) {
                    viewModel.displayedPosts.value = posts
                    drawMarkers()
                    viewModel.started.value = true
                }
                if (!editMode) {
                    viewModel.displayedPosts.value = posts
                    drawMarkers()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for (snapshot in snapshot.getChildren()) {
                    var Email = snapshot.child("Email").value.toString()
                    var UID = snapshot.child("UID").value.toString()
                    var Username = snapshot.child("Username").value.toString()

                    var user = User(Email, UID, Username)
                    users.add(user)
                }
                viewModel.users.value = users
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mMap != null) {
            viewModel.mapLatitude.value = mMap!!.cameraPosition.target.latitude
            viewModel.mapLongitude.value = mMap!!.cameraPosition.target.longitude
            viewModel.mapZoom.value = mMap!!.cameraPosition.zoom
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.moveCamera(CameraUpdateFactory
            .newLatLngZoom(LatLng(viewModel.mapLatitude.value!!,
                viewModel.mapLongitude.value!!),
                viewModel.mapZoom.value!!))

        mMap!!.setOnMapClickListener {
            if (editMode) {
                viewModel.currentLatitude.value = it.latitude
                viewModel.currentLongitude.value = it.longitude
                mMap!!.clear()
                mMap!!.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
            }
        }
        mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                for (i in 0 until viewModel.displayedPosts.value!!.size) {
                    if (viewModel.displayedPosts.value!![i].postUID == p0!!.title) {
                        viewModel.scrollToIndex.value = i
                        break
                    }
                }
                if (mMap != null) {
                    viewModel.mapLatitude.value = mMap!!.cameraPosition.target.latitude
                    viewModel.mapLongitude.value = mMap!!.cameraPosition.target.longitude
                    viewModel.mapZoom.value = mMap!!.cameraPosition.zoom
                }
                findNavController().navigate(R.id.action_MapsFragment_to_PostFragment)
                return true
            }
        })
        confirmFAB.setOnClickListener {
            if (viewModel.currentLatitude.value != null && viewModel.currentCaption.value != "") {
                var post = Post(
                    postsRef.push().key!!,
                    auth.uid!!,
                    currentPhotoPath,
                    viewModel.currentLatitude.value!!,
                    viewModel.currentLongitude.value!!,
                    currentCaption
                )
                var newPostRef = postsRef.push().setValue(post)

                posts.plus(post)
                viewModel.displayedPosts.value!!.add(post)
                viewModel.editMode.value = false
                viewModel.currentCaption.value = ""
                viewModel.currentLatitude.value = null
                viewModel.currentLongitude.value = null

                mMap!!.clear()
                normalView()
            }
            else if (viewModel.currentLatitude.value == null && viewModel.currentCaption.value == "") {
                Toast.makeText(context, "No location and caption input.",
                        Toast.LENGTH_SHORT).show()
            }
            else if (viewModel.currentLatitude.value == null) {
                Toast.makeText(context, "No location input.",
                        Toast.LENGTH_SHORT).show()
            }
            else if (viewModel.currentCaption.value == "") {
                Toast.makeText(context, "No caption input.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        clearFAB.setOnClickListener {
            mMap!!.clear()
            normalView()
            viewModel.editMode.value = false
        }

        listFAB.setOnClickListener {
            if (mMap != null) {
                viewModel.mapLatitude.value = mMap!!.cameraPosition.target.latitude
                viewModel.mapLongitude.value = mMap!!.cameraPosition.target.longitude
                viewModel.mapZoom.value = mMap!!.cameraPosition.zoom
            }
            viewModel.scrollToIndex.value = -1
            findNavController().navigate(R.id.action_MapsFragment_to_PostFragment)
        }

        if (editMode) {
            editView()
            if (viewModel.currentLatitude.value != null) {
                mMap!!.addMarker(MarkerOptions()
                    .position(LatLng(viewModel.currentLatitude.value!!, viewModel.currentLongitude.value!!)))
            }
        } else {
            normalView()
        }
        mMap!!.setOnMapLoadedCallback(this)
    }

    override fun onMapLoaded() {
        mMap?.moveCamera(CameraUpdateFactory
            .newLatLngZoom(LatLng(viewModel.mapLatitude.value!!,
                viewModel.mapLongitude.value!!),
                viewModel.mapZoom.value!!))
    }

    fun drawMarkers() {
        val size = viewModel.displayedPosts.value!!.size
        for (i in 0..(size-1)) {
            if (viewModel.displayedPosts.value!![i].userUID == auth.uid) {
                mMap?.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                viewModel.displayedPosts.value!![i].latitude,
                                viewModel.displayedPosts.value!![i].longitude
                            )
                        )
                        .title(viewModel.displayedPosts.value!![i].postUID)
                )
                    ?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            } else {
                mMap?.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                viewModel.displayedPosts.value!![i].latitude,
                                viewModel.displayedPosts.value!![i].longitude
                            )
                        )
                        .title(viewModel.displayedPosts.value!![i].postUID)
                )
                    ?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }
        }
    }

    fun editView() {
        listFAB.visibility = View.GONE
        instructionsTextView.visibility = View.VISIBLE
        captionEditText.visibility = View.VISIBLE
        captionsTextView.visibility = View.VISIBLE
        confirmFAB.visibility = View.VISIBLE
        clearFAB.visibility = View.VISIBLE
        cameraFAB.visibility = View.GONE
        crossHair.visibility = View.GONE
        if (mMap != null)
            mMap!!.clear()
    }

    fun normalView() {
        listFAB.visibility = View.VISIBLE
        instructionsTextView.visibility = View.GONE
        captionEditText.visibility = View.GONE
        captionsTextView.visibility = View.GONE
        confirmFAB.visibility = View.GONE
        clearFAB.visibility = View.GONE
        cameraFAB.visibility = View.VISIBLE
        crossHair.visibility = View.VISIBLE
        drawMarkers()
    }
}