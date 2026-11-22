package com.aprilarn.washflow.ui.features

import com.aprilarn.washflow.ui.aiagent.AiAgentAction
import com.aprilarn.washflow.ui.aiagent.AiAgentUiState
import com.aprilarn.washflow.ui.aiagent.AiAgentViewModel
import com.aprilarn.washflow.ui.analytics.AnalyticsUiState
import com.aprilarn.washflow.ui.analytics.AnalyticsViewModel
import com.aprilarn.washflow.ui.contributors.ContributorUiModel
import com.aprilarn.washflow.ui.contributors.ContributorsUiState
import com.aprilarn.washflow.ui.contributors.ContributorsViewModel
import com.aprilarn.washflow.ui.customers.CustomersUiState
import com.aprilarn.washflow.ui.customers.CustomersViewModel
import com.aprilarn.washflow.ui.home.HomeUiState
import com.aprilarn.washflow.ui.home.HomeViewModel
import com.aprilarn.washflow.ui.home.HourlyForecastUiState
import com.aprilarn.washflow.ui.items.ItemsUiState
import com.aprilarn.washflow.ui.items.ItemsViewModel
import com.aprilarn.washflow.ui.login.LoginUiState
import com.aprilarn.washflow.ui.login.LoginViewModel
import com.aprilarn.washflow.ui.login.UserData
import com.aprilarn.washflow.ui.main.MainNavigationEvent
import com.aprilarn.washflow.ui.main.MainUiState
import com.aprilarn.washflow.ui.main.MainViewModel
import com.aprilarn.washflow.ui.manage_order.ManageOrderUiState
import com.aprilarn.washflow.ui.manage_order.ManageOrderViewModel
import com.aprilarn.washflow.ui.notifications.NotificationsUiState
import com.aprilarn.washflow.ui.notifications.NotificationsViewModel
import com.aprilarn.washflow.ui.orders.OrdersUiState
import com.aprilarn.washflow.ui.orders.OrdersViewModel
import com.aprilarn.washflow.ui.services.ServicesUiState
import com.aprilarn.washflow.ui.services.ServicesViewModel
import com.aprilarn.washflow.ui.settings.SettingsUiState
import com.aprilarn.washflow.ui.settings.SettingsViewModel
import com.aprilarn.washflow.ui.tabledata.TableDataUiState
import com.aprilarn.washflow.ui.tabledata.TableDataViewModel
import com.aprilarn.washflow.ui.workspace.WorkspaceEvent
import com.aprilarn.washflow.ui.workspace.WorkspaceUiState
import com.aprilarn.washflow.ui.workspace.WorkspaceViewModel

/**
 * Shared Presentation Layer (ui/features)
 * Menyatukan seluruh ViewModel & UiState lintas fitur sehingga dapat digunakan secara konsisten
 * baik oleh folder UI khusus HP (ui/phone) maupun folder UI khusus Tablet (ui/tablet).
 */
typealias FeatureHomeViewModel = HomeViewModel
typealias FeatureHomeUiState = HomeUiState
typealias FeatureHourlyForecastUiState = HourlyForecastUiState

typealias FeatureOrdersViewModel = OrdersViewModel
typealias FeatureOrdersUiState = OrdersUiState

typealias FeatureManageOrderViewModel = ManageOrderViewModel
typealias FeatureManageOrderUiState = ManageOrderUiState

typealias FeatureAnalyticsViewModel = AnalyticsViewModel
typealias FeatureAnalyticsUiState = AnalyticsUiState

typealias FeatureTableDataViewModel = TableDataViewModel
typealias FeatureTableDataUiState = TableDataUiState

typealias FeatureCustomersViewModel = CustomersViewModel
typealias FeatureCustomersUiState = CustomersUiState

typealias FeatureServicesViewModel = ServicesViewModel
typealias FeatureServicesUiState = ServicesUiState

typealias FeatureItemsViewModel = ItemsViewModel
typealias FeatureItemsUiState = ItemsUiState

typealias FeatureSettingsViewModel = SettingsViewModel
typealias FeatureSettingsUiState = SettingsUiState

typealias FeatureContributorsViewModel = ContributorsViewModel
typealias FeatureContributorsUiState = ContributorsUiState
typealias FeatureContributorUiModel = ContributorUiModel

typealias FeatureLoginViewModel = LoginViewModel
typealias FeatureLoginUiState = LoginUiState
typealias FeatureUserData = UserData

typealias FeatureWorkspaceViewModel = WorkspaceViewModel
typealias FeatureWorkspaceUiState = WorkspaceUiState
typealias FeatureWorkspaceEvent = WorkspaceEvent

typealias FeatureMainViewModel = MainViewModel
typealias FeatureMainUiState = MainUiState
typealias FeatureMainNavigationEvent = MainNavigationEvent

typealias FeatureNotificationsViewModel = NotificationsViewModel
typealias FeatureNotificationsUiState = NotificationsUiState

typealias FeatureAiAgentViewModel = AiAgentViewModel
typealias FeatureAiAgentUiState = AiAgentUiState
typealias FeatureAiAgentAction = AiAgentAction
