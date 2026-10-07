package com.sos.studentonstudy.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sos.studentonstudy.AppContainer
import com.sos.studentonstudy.SosApplication
import com.sos.studentonstudy.ui.auth.LoginViewModel
import com.sos.studentonstudy.ui.auth.RegisterViewModel
import com.sos.studentonstudy.ui.dashboard.DashboardViewModel
import com.sos.studentonstudy.ui.explore.ExploreViewModel
import com.sos.studentonstudy.ui.profile.ProfileViewModel
import com.sos.studentonstudy.ui.request.RequestFormViewModel

/** Builds every ViewModel from the app's [AppContainer]; UI never touches DAOs directly. */
val SosViewModelFactory = viewModelFactory {
    initializer { LoginViewModel(container().authRepository) }
    initializer { RegisterViewModel(container().authRepository) }
    initializer { DashboardViewModel(container().authRepository, container().requestRepository) }
    initializer { ExploreViewModel(container().requestRepository) }
    initializer { RequestFormViewModel(createSavedStateHandle(), container().requestRepository, container().authRepository) }
    initializer { ProfileViewModel(container().authRepository) }
}

private fun CreationExtras.container(): AppContainer = (this[APPLICATION_KEY] as SosApplication).container
