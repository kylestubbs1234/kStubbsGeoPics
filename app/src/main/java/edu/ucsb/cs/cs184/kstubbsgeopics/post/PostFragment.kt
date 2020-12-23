package edu.ucsb.cs.cs184.kstubbsgeopics.post

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.ucsb.cs.cs184.kstubbsgeopics.R
import edu.ucsb.cs.cs184.kstubbsgeopics.maps.MapsViewModel
import kotlin.math.pow


class PostFragment : Fragment() {

    companion object {
        fun newInstance() = PostFragment()
    }

    private lateinit var linearLayoutManager: LinearLayoutManager
    val viewModel: MapsViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view =  inflater.inflate(R.layout.fragment_post, container, false)

        setHasOptionsMenu(true)

        linearLayoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.recyclerView)

        if (viewModel.scrollToIndex.value!! == -1) {
            viewModel.scrollToIndex.value = 0
        } else {
            if (viewModel.totalPosts.value!!.size > 0) {
                var valueAtIndexBeforeSort = viewModel.totalPosts.value?.get(viewModel.scrollToIndex.value!!)
                sortDisplayedItemsByDistance()
                for (i in 0 until viewModel.totalPosts.value!!.size) {
                    if (valueAtIndexBeforeSort == viewModel.totalPosts.value!![i]) {
                        viewModel.scrollToIndex.value = i
                        break
                    }
                }
            }

        }

        linearLayoutManager.scrollToPosition(viewModel.scrollToIndex.value!!)

        recyclerView.layoutManager = linearLayoutManager

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        return view
    }

    fun sortDisplayedItemsByDistance() {
        viewModel.distances.value!!.clear()
        for (i in 0 until viewModel.totalPosts.value!!.size) {
            var minDistance = Double.MAX_VALUE
            var minIndex = 0
            for (j in i until viewModel.totalPosts.value!!.size) {
                var distance = getDistance(viewModel.totalPosts.value!![j])
                if (distance < minDistance) {
                    minDistance = distance
                    minIndex = j
                }
            }
            viewModel.distances.value!!.add(minDistance)
            var tempPost: Post = viewModel.totalPosts.value!![i]
            viewModel.totalPosts.value!![i] = viewModel.totalPosts.value!![minIndex]
            viewModel.totalPosts.value!![minIndex] = tempPost
        }
        viewModel.distances.value!!.sort()
    }

    fun getDistance(post: Post): Double {
        var xDiff = post.latitude - viewModel.mapLatitude.value!!
        var yDiff = post.longitude - viewModel.mapLongitude.value!!
        var distance = Math.sqrt(xDiff.pow(2) + yDiff.pow(2))
        return distance
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.refresh, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_refresh) {
            sortDisplayedItemsByDistance()
            postAdapter.notifyDataSetChanged()
        }
        return true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sortDisplayedItemsByDistance()
        postAdapter = PostAdapter(context, viewModel.totalPosts.value!!, viewModel.users.value!!, viewModel.distances.value)
        recyclerView.adapter = postAdapter

    }

}