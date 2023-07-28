package com.unero.pinar.di

import com.unero.pinar.data.repository.AuthRepositoryImpl
import com.unero.pinar.data.repository.BuildingRepositoryImpl
import com.unero.pinar.data.repository.PathNodeRepositoryImpl
import com.unero.pinar.data.repository.ReportRepositoryImpl
import com.unero.pinar.domain.repository.AuthRepository
import com.unero.pinar.domain.repository.BuildingRepository
import com.unero.pinar.domain.repository.PathNodeRepository
import com.unero.pinar.domain.repository.ReportRepository
import com.unero.pinar.presentation.detail.add.AddSceneViewModel
import com.unero.pinar.presentation.home.HomeViewModel
import com.unero.pinar.presentation.home.add.HomeFormViewModel
import com.unero.pinar.presentation.maps.MapsViewModel
import com.unero.pinar.presentation.scan.ScanViewModel
import com.unero.pinar.presentation.scene.SceneViewModel
import com.unero.pinar.presentation.sharedviewmodel.NavigationViewModel
import com.unero.pinar.presentation.tracktest.TrackTestViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    // Single instance of Repository
    single<AuthRepository> { AuthRepositoryImpl() }
    single<BuildingRepository> { BuildingRepositoryImpl() }
    single<PathNodeRepository> { PathNodeRepositoryImpl() }

    // Data for Testing
    single<ReportRepository> { ReportRepositoryImpl() }
}

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { HomeFormViewModel(get()) }
    viewModel { MapsViewModel(get()) }
    viewModel { NavigationViewModel(get()) }
    viewModel { SceneViewModel() }
    viewModel { AddSceneViewModel(get(), get()) }
    viewModel { ScanViewModel() }
    viewModel { TrackTestViewModel(get()) }
}