package com.pathfinder.khushu.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pathfinder.khushu.MainViewModel
import com.pathfinder.khushu.R
import com.pathfinder.khushu.databinding.FragmentHomeBinding
import com.pathfinder.khushu.lib.MainViewModelFactory
import com.pathfinder.khushu.utils.PreferencesRepository


class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: PlacesRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences =
            requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)
        val viewModelFactory = MainViewModelFactory(preferencesRepository, requireContext())

        mainViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(MainViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PlacesRecyclerAdapter(
            mainViewModel.places.value.orEmpty(),
            onDeleteClick = { place -> mainViewModel.removePlace(place) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        mainViewModel.places.observe(viewLifecycleOwner) { places ->
            adapter.updatePlaces(places)
            if (places.isEmpty()) {
                binding.emptyMessage.visibility = View.VISIBLE
            } else {
                binding.emptyMessage.visibility = View.GONE

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}