import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ICountry } from '../country.model';
import { CountryService } from '../service/country.service';
import { CountryDeleteDialogComponent } from '../delete/country-delete-dialog.component';

@Component({
  selector: 'jhi-country',
  templateUrl: './country.component.html',
})
export class CountryComponent implements OnInit {
  countries?: ICountry[];
  isLoading = false;
  currentSearch: string;

  constructor(protected countryService: CountryService, protected modalService: NgbModal, protected activatedRoute: ActivatedRoute) {
    this.currentSearch = this.activatedRoute.snapshot.queryParams['search'] ?? '';
  }

  loadAll(): void {
    this.isLoading = true;
    if (this.currentSearch) {
      this.countryService
        .search({
          query: this.currentSearch,
        })
        .subscribe({
          next: (res: HttpResponse<ICountry[]>) => {
            this.isLoading = false;
            this.countries = res.body ?? [];
          },
          error: () => {
            this.isLoading = false;
          },
        });
      return;
    }

    this.countryService.query().subscribe({
      next: (res: HttpResponse<ICountry[]>) => {
        this.isLoading = false;
        this.countries = res.body ?? [];
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  search(query: string): void {
    this.currentSearch = query;
    this.loadAll();
  }

  ngOnInit(): void {
    this.loadAll();
  }

  trackId(index: number, item: ICountry): number {
    return item.id!;
  }

  delete(country: ICountry): void {
    const modalRef = this.modalService.open(CountryDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.country = country;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.loadAll();
      }
    });
  }
}
