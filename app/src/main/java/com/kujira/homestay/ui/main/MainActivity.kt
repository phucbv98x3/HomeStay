package com.kujira.homestay.ui.main

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import com.kujira.homestay.R
import com.kujira.homestay.databinding.ActivityMainBinding
import com.kujira.homestay.ui.account.AccountFragment
import com.kujira.homestay.ui.base.BaseActivity
import com.kujira.homestay.ui.base.BaseFragment
import com.kujira.homestay.ui.home.HomeFragment
import com.kujira.homestay.ui.manager.ManagerRoomFragment
import com.kujira.homestay.utils.printLog
import kotlinx.android.synthetic.main.activity_main.*


open class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>(),
    NavController.OnDestinationChangedListener {
    private lateinit var navController: NavController
    private lateinit var currentFragment: BaseFragment<*, *>
    private var currentFragmentId: Int = R.id.home_fragment
    private val navHostFragment: NavHostFragment by lazy {
        supportFragmentManager.findFragmentById(R.id.main_nav_fragment) as NavHostFragment
    }

    override fun onSupportNavigateUp(): Boolean {
        currentFragment.onNavigationUp()
        return false
    }

    override fun createViewModel(): Class<MainViewModel> {
        return MainViewModel::class.java
    }

    override fun getContentView(): Int {
        return R.layout.activity_main
    }

    override fun initAction() {
    }

    override fun initData() {
        listenerAction()
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        navController = navHostFragment.navController
//        setSupportActionBar(toolbar)
        navController.addOnDestinationChangedListener(this)
        //NavigationUI.setupActionBarWithNavController(this, navController)
    }

    private fun listenerAction() {


        mViewModel.btnClick.observe(this, {
            when (it) {
                MainViewModel.BTN_HOME -> {
                    if (currentFragment is HomeFragment){

                    }else{
                        navigate(R.id.home_fragment)
                    }

                }

                MainViewModel.BTN_DATCHO -> {
                    if (currentFragment is ManagerRoomFragment){
                    }else{
                        navigate(R.id.manager_Room_fragment)
                    }
                }

                MainViewModel.BTN_ACCOUNT -> {
                    if (currentFragment is AccountFragment){
                    }else{
                        navigate(R.id.account_fragment)
                    }
                }
            }
        })

    }


    override fun navigateUp() {
        val isFinish = !navController.popBackStack()
        if (isFinish) {
            finish()
        }
    }

    override fun onFragmentResumed(fragment: BaseFragment<*, *>) {
        currentFragment = fragment
        printLog("currentFragment: ${fragment::class.simpleName}")
    }

    private fun getNavOptionsNavigate(): NavOptions.Builder? {
        return NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out)
    }

    override fun navigate(
        fragmentId: Int,
        bundle: Bundle?,
        addToBackStack: Boolean
    ) {
        val navOptions = getNavOptionsNavigate()
        if (!addToBackStack) {
            navOptions?.setPopUpTo(currentFragmentId, true)
        }
        navController.navigate(fragmentId, bundle, navOptions?.build())
        currentFragmentId = fragmentId
    }


    override fun navigateWithSharedElement(
        fragmentId: Int,
        bundle: Bundle?,
        sharedElements: FragmentNavigator.Extras?,
        addToBackStack: Boolean
    ) {

        val navOptions = getNavOptionsNavigate()
        if (!addToBackStack) {
            navOptions?.setPopUpTo(currentFragmentId, true)
        }
        navController.navigate(fragmentId, bundle, null, sharedElements)
    }

    override fun present(fragmentId: Int, bundle: Bundle?) {
        currentFragmentId = fragmentId
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()


    }
}