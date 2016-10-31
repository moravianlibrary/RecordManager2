import {StatisticsComponent} from "./statistics.component";
import {ModuleWithProviders} from "@angular/core";
import {RouterModule} from "@angular/router";
import {StatisticDetailComponent} from "./statistic-detail/statistic-detail.component";
const statisticsRoutes = [
  {
    path: 'oaiHarvestStats',
    component: StatisticsComponent,
  },
  {
    path: 'oaiHarvestStats/:id',
    component: StatisticDetailComponent,
  }
];
export const statisticsRouting : ModuleWithProviders = RouterModule.forChild(statisticsRoutes);
