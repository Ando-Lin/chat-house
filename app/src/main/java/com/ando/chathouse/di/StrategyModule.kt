package com.ando.chathouse.di

import com.ando.chathouse.strategy.*
import com.ando.chathouse.strategy.impl.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object StrategyModule {
    @Provides
    fun provideCarryMessageStrategyManager() = CarryMessageStrategyManagerImpl().apply {
            addStrategy(PreferMeCarryMessageStrategy.NAME, PreferMeCarryMessageStrategy::class.java)
            addStrategy(FixedWindowCarryMessageStrategy.NAME, FixedWindowCarryMessageStrategy::class.java )
            addStrategy(GreedyCarryMessageStrategy.NAME, GreedyCarryMessageStrategy::class.java)
            addStrategy(NoCarryMessageStrategy.NAME, NoCarryMessageStrategy::class.java)
        }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StrategyMangerModule {
    @Binds
    abstract fun bindStrategyManger(impl: CarryMessageStrategyManagerImpl): CarryMessageStrategyManager
}