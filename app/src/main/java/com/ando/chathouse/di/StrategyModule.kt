package com.ando.chathouse.di

import android.content.Context
import com.ando.chathouse.R
import com.ando.chathouse.domain.pojo.StrategyMap
import com.ando.chathouse.strategy.*
import com.ando.chathouse.strategy.impl.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object StrategyModule {
    @Provides
    fun provideCarryMessageStrategyManager() = CarryMessageStrategyManagerImpl().apply {
            addStrategyByClassName(PreferMeCarryMessageStrategy::class.java)
            addStrategyByClassName(FixedWindowCarryMessageStrategy::class.java)
            addStrategyByClassName(GreedyCarryMessageStrategy::class.java)
            addStrategyByClassName(NoCarryMessageStrategy::class.java)
        }

    @Provides
    fun provideStrategyList(@ApplicationContext context:Context):StrategyMap{
        val map = mapOf(
            context.getString(R.string.prefer_me) to PreferMeCarryMessageStrategy::class.simpleName!!,
            context.getString(R.string.fixed_window) to FixedWindowCarryMessageStrategy::class.simpleName!!,
            context.getString(R.string.greedy) to GreedyCarryMessageStrategy::class.simpleName!!,
            context.getString(R.string.no_carry) to NoCarryMessageStrategy::class.simpleName!!
        )
        return StrategyMap(map)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StrategyMangerModule {
    @Binds
    abstract fun bindStrategyManger(impl: CarryMessageStrategyManagerImpl): CarryMessageStrategyManager
}