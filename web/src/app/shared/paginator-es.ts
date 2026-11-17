import { MatPaginatorIntl } from '@angular/material/paginator';

/** Textos del paginador en español. */
export function paginatorEs(): MatPaginatorIntl {
  const p = new MatPaginatorIntl();
  p.itemsPerPageLabel = 'Filas por página';
  p.nextPageLabel = 'Página siguiente';
  p.previousPageLabel = 'Página anterior';
  p.firstPageLabel = 'Primera página';
  p.lastPageLabel = 'Última página';
  p.getRangeLabel = (page, size, length) => {
    if (length === 0 || size === 0) return `0 de ${length}`;
    const start = page * size;
    const end = Math.min(start + size, length);
    return `${start + 1} – ${end} de ${length}`;
  };
  return p;
}
