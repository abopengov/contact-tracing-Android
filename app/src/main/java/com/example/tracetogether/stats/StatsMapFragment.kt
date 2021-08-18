package com.example.tracetogether.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import kotlinx.android.synthetic.main.fragment_stats_map.*
import kotlinx.android.synthetic.main.fragment_stats_map.toolbar

class StatsMapFragment : Fragment(), OnMapReadyCallback {
    companion object {
        const val TAG = "StatsMapFragment"
    }

    private val statsViewModel: StatsViewModel by viewModels()
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stats_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.title = "stats_map".getLocalizedText()
        toolbar.setNavigationOnClickListener { goBack() }
        legend_title.setLocalizedString("stats_active_cases")

        setupLiveData()

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.stats_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    private fun setupLiveData() {
        statsViewModel.networkError.observe(viewLifecycleOwner, Observer { networkError ->
            if (networkError) {
                Snackbar.make(coordinator_layout, "error_cannot_fetch_data".getLocalizedText(), Snackbar.LENGTH_SHORT)
                        .show()
            }
        })

        statsViewModel.municipalityStats.observe(viewLifecycleOwner, Observer { municipalityStats ->
            val layer = GeoJsonLayer(map, R.raw.ab_counties, context).apply {
                defaultPolygonStyle.strokeWidth = 1f
            }

            for (feature in layer.features) {
                val stats = municipalityStats.find {
                    it.municipality.equals(feature.getProperty("GEONAME"), ignoreCase = true)
                }

                stats?.let {
                    feature.polygonStyle = GeoJsonPolygonStyle().apply {
                        fillColor = getColorForActiveCases(it.activeCases)
                        strokeWidth = layer.defaultPolygonStyle.strokeWidth
                    }
                } ?: run {
                    feature.polygonStyle = GeoJsonPolygonStyle().apply {
                        fillColor = getColorForActiveCases(0.0)
                        strokeWidth = layer.defaultPolygonStyle.strokeWidth
                    }
                }
            }

            layer.addLayerToMap()
        })
    }

    private fun goBack() {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        fragManager?.popBackStack()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        configureMap()

        statsViewModel.getMunicipalityStats()
    }

    private fun configureMap() {
        val bounds = LatLngBounds.builder()
                .include(LatLng(48.99, -120.00))
                .include(LatLng(60.00, -109.99))
                .build()

        try {
            map.moveCamera((CameraUpdateFactory.newLatLngBounds(bounds, 20)))
            map.setLatLngBoundsForCameraTarget(bounds)
            map.setMinZoomPreference(map.cameraPosition.zoom)
        } catch (ex: Exception) {
            CentralLog.e(TAG, "Failed to update map " + ex.message)
        }
    }

    private fun getColorForActiveCases(activeCases: Double?): Int {
        return when {
            activeCases == null -> {
                ContextCompat.getColor(requireContext(), R.color.map_cases_none)
            }
            activeCases >= 100 -> {
                ContextCompat.getColor(requireContext(), R.color.map_cases_high)
            }
            activeCases >= 10 -> {
                ContextCompat.getColor(requireContext(), R.color.map_cases_medium)
            }
            activeCases >= 1 -> {
                ContextCompat.getColor(requireContext(), R.color.map_cases_low)
            }
            else -> {
                ContextCompat.getColor(requireContext(), R.color.map_cases_none)
            }
        }
    }
}
