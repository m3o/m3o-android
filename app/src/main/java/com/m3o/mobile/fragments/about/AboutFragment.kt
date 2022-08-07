package com.m3o.mobile.fragments.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m3o.mobile.BuildConfig
import com.m3o.mobile.R
import com.m3o.mobile.activities.MainActivity
import com.m3o.mobile.utils.openUrl
import com.mikepenz.aboutlibraries.LibsBuilder
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return AboutPage(context, R.style.Theme_AboutPage)
            .setDescription(getString(R.string.about_description))
            .addItem(
                Element(
                    String.format(
                        getString(R.string.about_element_version),
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_github
                ).setOnClickListener { openUrl(getString(R.string.about_releases_link)) }
            )
            .addGroup(getString(R.string.about_group_attribution))
            .addItem(
                Element(
                    getString(R.string.about_element_libraries),
                    R.drawable._ic_libraries
                ).setOnClickListener { showLibraries() }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_icons),
                    R.drawable._ic_question
                ).setOnClickListener { showIcons() }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_animations),
                    R.drawable._ic_question
                ).setOnClickListener { showAnimations() }
            )
            .addGroup(getString(R.string.about_group_links))
            .addItem(
                Element(
                    getString(R.string.about_element_website),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_link
                ).setOnClickListener { openUrl(getString(R.string.about_website_link)) }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_discord),
                    R.drawable.ic_discord
                ).setOnClickListener { openUrl(getString(R.string.about_discord_link)) }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_twitter),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_twitter
                ).setOnClickListener { openUrl(getString(R.string.about_twitter_link)) }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_instagram),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_instagram
                ).setOnClickListener { openUrl(getString(R.string.about_instagram_link)) }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_github),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_github
                ).setOnClickListener { openUrl(getString(R.string.about_github_link)) }
            )
            .addItem(
                Element(
                    getString(R.string.about_element_playstore),
                    mehdi.sakout.aboutpage.R.drawable.about_icon_google_play
                ).setOnClickListener { openUrl(getString(R.string.about_play_store_link)) }
            )
            .create()
    }

    private fun showLibraries() {
        context?.let { validContext ->
            LibsBuilder()
                .withLicenseShown(true)
                .withAboutIconShown(false)
                .withAboutVersionShown(false)
                .withActivityTitle(getString(R.string.about_element_libraries))
                .withSearchEnabled(true)
                .start(validContext)
        }
    }

    private fun showIcons() {
        findNavController().navigate(R.id.AboutIconsFragment)
    }

    private fun showAnimations() {
        findNavController().navigate(R.id.AboutAnimationsFragment)
    }

    override fun onStart() {
        super.onStart()
        activity.let {
            (it as MainActivity).hideFab()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.let {
            (it as MainActivity).showFab()
        }
    }
}
