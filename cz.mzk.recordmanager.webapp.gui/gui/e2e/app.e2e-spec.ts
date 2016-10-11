import { GuiPage } from './app.po';

describe('gui App', function() {
  let page: GuiPage;

  beforeEach(() => {
    page = new GuiPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
