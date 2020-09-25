package gr.cpaleop.dashboard.di

import gr.cpaleop.core.data.mappers.DocumentMapper
import gr.cpaleop.core.dispatchers.DefaultDispatcher
import gr.cpaleop.core.dispatchers.IODispatcher
import gr.cpaleop.core.dispatchers.MainDispatcher
import gr.cpaleop.core.domain.DateFormatter
import gr.cpaleop.core.domain.DateFormatterImpl
import gr.cpaleop.dashboard.data.*
import gr.cpaleop.dashboard.data.mappers.AnnouncementMapper
import gr.cpaleop.dashboard.data.mappers.CategoryMapper
import gr.cpaleop.dashboard.data.mappers.NotificationMapper
import gr.cpaleop.dashboard.data.mappers.ProfileMapper
import gr.cpaleop.dashboard.data.remote.NotificationsApi
import gr.cpaleop.dashboard.data.remote.ProfileApi
import gr.cpaleop.dashboard.domain.repositories.*
import gr.cpaleop.dashboard.domain.usecases.*
import gr.cpaleop.dashboard.presentation.announcements.AnnouncementPresentationMapper
import gr.cpaleop.dashboard.presentation.announcements.AnnouncementPresentationMapperImpl
import gr.cpaleop.dashboard.presentation.announcements.AnnouncementsViewModel
import gr.cpaleop.dashboard.presentation.announcements.categoryfilterdialog.CategoryFilterMapper
import gr.cpaleop.dashboard.presentation.announcements.categoryfilterdialog.CategoryFilterViewModel
import gr.cpaleop.dashboard.presentation.documents.DocumentsViewModel
import gr.cpaleop.dashboard.presentation.documents.document.FileDocumentMapper
import gr.cpaleop.dashboard.presentation.documents.options.DocumentOptionMapper
import gr.cpaleop.dashboard.presentation.documents.sort.DocumentSortOptionMapper
import gr.cpaleop.dashboard.presentation.documents.sort.DocumentSortOptionsViewModel
import gr.cpaleop.dashboard.presentation.notifications.NotificationPresentationMapper
import gr.cpaleop.dashboard.presentation.notifications.NotificationsViewModel
import gr.cpaleop.dashboard.presentation.notifications.categories.CategoriesFilterViewModel
import gr.cpaleop.dashboard.presentation.profile.ProfilePresentationMapper
import gr.cpaleop.dashboard.presentation.profile.ProfilePresentationMapperImpl
import gr.cpaleop.dashboard.presentation.profile.ProfileViewModel
import gr.cpaleop.dashboard.presentation.profile.options.SelectedSocialOptionMapper
import gr.cpaleop.dashboard.presentation.profile.options.SelectedSocialOptionMapperImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

@ExperimentalCoroutinesApi
val dashboardModule = module {
    viewModel { CategoryFilterViewModel(get(), get()) }
    viewModel { CategoriesFilterViewModel(get(), get()) }
    viewModel { AnnouncementsViewModel(get(named<MainDispatcher>()), get(), get(), get()) }
    viewModel {
        NotificationsViewModel(
            get(named<MainDispatcher>()),
            get(named<DefaultDispatcher>()),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        DocumentsViewModel(
            get(named<MainDispatcher>()),
            get(named<DefaultDispatcher>()),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        DocumentSortOptionsViewModel(
            get(named<MainDispatcher>()),
            get(named<DefaultDispatcher>()),
            get(),
            get(),
            get()
        )
    }
    viewModel { ProfileViewModel(get(named<MainDispatcher>()), get(), get(), get(), get()) }
    single { NotificationPresentationMapper(get()) }
    single { DocumentSortOptionMapper() }
    single { DocumentOptionMapper() }
    single<SelectedSocialOptionMapper> { SelectedSocialOptionMapperImpl() }
    single<ProfilePresentationMapper> {
        ProfilePresentationMapperImpl(
            get(),
            get(named<DefaultDispatcher>())
        )
    }
    single<AnnouncementPresentationMapper> { AnnouncementPresentationMapperImpl(get()) }
    single<DateFormatter> { DateFormatterImpl() }
    single { CategoryFilterMapper() }
    single { FileDocumentMapper(get(), get(named<DefaultDispatcher>()), get()) }
    single { AnnouncementMapper() }
    single { NotificationMapper(get(named<DefaultDispatcher>())) }
    single { ProfileMapper(get()) }
    single { DocumentMapper(get(named<DefaultDispatcher>())) }
    single { CategoryMapper() }
    single<ToggleDocumentPreviewPreferenceUseCase> { ToggleDocumentPreviewPreferenceUseCaseImpl(get()) }
    single<GetDocumentPreviewPreferenceUseCase> { GetDocumentPreviewPreferenceUseCaseImpl(get()) }
    single<ObserveDocumentsAnnouncementFoldersUseCase> {
        ObserveDocumentsAnnouncementFoldersUseCaseImpl(
            get(named<DefaultDispatcher>()), get(), get(), get()
        )
    }
    single<ReadAllNotificationsUseCase> { ReadAllNotificationsUseCaseImpl(get()) }
    single<FilterAnnouncementsUseCase> { FilterAnnouncementsUseCaseImpl(get()) }
    single<ObserveDocumentSortUseCase> { ObserveDocumentSortUseCaseImpl(get()) }
    single<GetDocumentSortOptionsUseCase> { GetDocumentSortOptionsUseCaseImpl(get()) }
    single<RenameDocumentUseCase> { RenameDocumentUseCaseImpl(get()) }
    single<DeleteDocumentUseCase> { DeleteDocumentUseCaseImpl(get()) }
    single<GetDocumentUseCase> { GetDocumentUseCaseImpl(get()) }
    single<UpdateSocialUseCase> { UpdateSocialUseCaseImpl(get()) }
    single<GetDocumentOptionsUseCase> { GetDocumentOptionsUseCaseImpl() }
    single<GetCachedCategoriesUseCase> { GetCachedCategoriesUseCaseImpl(get()) }
    single<UpdateRegisteredCategoriesUseCase> { UpdateRegisteredCategoriesUseCaseImpl(get()) }
    single<GetCategoriesUseCase> { GetCategoriesUseCaseImpl(get()) }
    single<ObserveDocumentsUseCase> { ObserveDocumentsUseCaseImpl(get(), get(), get()) }
    single<GetProfileUseCase> { GetProfileUseCaseImpl(get()) }
    single<GetNotificationsUseCase> { GetNotificationsUseCaseImpl(get()) }
    single<ObserveAnnouncementsUseCase> { ObserveAnnouncementsUseCaseImpl(get()) }
    single<CategoriesRepository> { CategoriesRepositoryImpl(get(), get(), get(), get()) }
    single<DeviceStorageRepository> {
        DeviceStorageRepositoryImpl(
            get(named<IODispatcher>()),
            get(),
            get()
        )
    }
    single<NotificationsRepository> { NotificationsRepositoryImpl(get(), get()) }
    single<AnnouncementsRepository> {
        AnnouncementsRepositoryImpl(
            get(named<IODispatcher>()),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<PreferencesRepository> { PreferencesRepositoryImpl(get(named<IODispatcher>()), get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single { provideNotificationsApi(get()) }
    single { provideProfileApi(get()) }
}

private fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi {
    return retrofit.create(NotificationsApi::class.java)
}

private fun provideProfileApi(retrofit: Retrofit): ProfileApi {
    return retrofit.create(ProfileApi::class.java)
}