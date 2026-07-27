import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {QdApplicationRoleFilter, QdAuthorizationGuard} from '@quadrel-enterprise-ui/auth';
import {readQdRoleFilter} from "./shared/common.constants";
import {ClaimsComponent} from "./pages/claims-test-page/claims.component";

const routes: Routes = [
  {
    path: 'user',
    canActivate: [QdAuthorizationGuard],
    data: {
      roleFilter: [QdApplicationRoleFilter.hasRole(readQdRoleFilter)]
    },
    component: ClaimsComponent
  },
  {
    path: 'redirect',
    redirectTo: 'user',
    pathMatch: 'full'
  },
  {
    path: '',
    redirectTo: 'user',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: ''
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
