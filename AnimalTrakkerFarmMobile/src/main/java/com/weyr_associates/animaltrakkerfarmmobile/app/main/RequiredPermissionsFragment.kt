package com.weyr_associates.animaltrakkerfarmmobile.app.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.weyr_associates.animaltrakkerfarmmobile.R
import com.weyr_associates.animaltrakkerfarmmobile.app.permissions.RequiredPermissionsActivity

class RequiredPermissionsFragment : Fragment(R.layout.fragment_required_permissions) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.requireViewById<Button>(R.id.button_open_required_permissions).setOnClickListener {
            startActivity(Intent(requireActivity(), RequiredPermissionsActivity::class.java))
        }
    }
}
