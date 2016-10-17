export class ContactPerson {
  id: number;
  name: string;
  email: string;
  phone: string;

  constructor(obj?: any)
  {
    this.id          = obj && obj.id             || null;
    this.name        = obj && obj.name           || null;
    this.email       = obj && obj.email          || null;
    this.phone       = obj && obj.phone          || null;
  }
}
