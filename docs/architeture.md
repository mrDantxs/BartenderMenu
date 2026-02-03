# Arquitetura do Projeto

O projeto BartenderMenu segue uma organização baseada em pacotes por responsabilidade.

## Estrutura de Pacotes (Java)

com.will.bartendermenu

- ui
    - main → MainActivity
    - intro → IntroSlideActivity
    - drink → AddDrinkActivity, DrinkListActivity, DrinkAdapter
    - tasting → TastingActivity
    - result → ResultActivity

- model
    - Drink → modelo de dados do drink

- database
    - DatabaseHelper → gerenciamento do banco de dados local

- utils
    - FileUtils → criação, verificação e exclusão de arquivos de imagem
    - PdfGenerator → geração de PDFs com informações dos drinks

## Estrutura de Layouts (XML)

Os layouts foram organizados em diretórios de recursos específicos:

- layout_main
- layout_intro
- layout_drink
- layout_tasting
- layout_result
- layout_common (layouts reutilizáveis, como include_back.xml)
- layout-land (versões para modo paisagem)
# Arquitetura do Projeto

O projeto BartenderMenu segue uma organização baseada em pacotes por responsabilidade.

## Estrutura de Pacotes (Java)

com.will.bartendermenu

- ui
    - main → MainActivity
    - intro → IntroSlideActivity
    - drink → AddDrinkActivity, DrinkListActivity, DrinkAdapter
    - tasting → TastingActivity
    - result → ResultActivity

- model
    - Drink → modelo de dados do drink

- database
    - DatabaseHelper → gerenciamento do banco de dados local

- utils
    - FileUtils → criação, verificação e exclusão de arquivos de imagem
    - PdfGenerator → geração de PDFs com informações dos drinks

## Estrutura de Layouts (XML)

Os layouts foram organizados em diretórios de recursos específicos:

- layout_main
- layout_intro
- layout_drink
- layout_tasting
- layout_result
- layout_common (layouts reutilizáveis, como include_back.xml)
- layout-land (versões para modo paisagem)
