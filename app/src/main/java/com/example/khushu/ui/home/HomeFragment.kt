package com.example.khushu.ui.home

import ItemAdapter
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.khushu.R
import com.example.khushu.databinding.FragmentHomeBinding
import com.example.khushu.lib.HomeViewModelFactory
import com.example.khushu.utils.PreferencesRepository

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val preferencesRepository = PreferencesRepository(sharedPreferences)
        val factory = HomeViewModelFactory(preferencesRepository)

        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        // Set up RecyclerView
        val adapter = ItemAdapter(homeViewModel)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe the ViewModel's items
        homeViewModel.items.observe(viewLifecycleOwner, Observer { items ->
            if (items != null) {
                adapter.submitList(items)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}