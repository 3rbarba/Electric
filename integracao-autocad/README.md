# Integração com AutoCAD

O complemento oferece dois métodos: indicar pontos ao redor do cômodo (sem exigir um objeto fechado) ou selecionar uma polilinha fechada. As medidas são enviadas em metros ao ELETRIC por HTTP local. O tipo e o nome do cômodo continuam sendo escolhidos no aplicativo.

## Compilar

Instale o .NET 8 SDK para o AutoCAD 2026 e o .NET Framework 4.8 Developer Pack para o AutoCAD 2023. Os assemblies gerenciados do AutoCAD (`AcCoreMgd.dll`, `AcDbMgd.dll` e `AcMgd.dll`) precisam estar no diretório padrão de instalação ou em um diretório indicado por `AutoCADInstallDir`.

No PowerShell, a partir desta pasta:

```powershell
dotnet build .\EletricAutoCAD.csproj -c Release -p:AutoCADVersion=2023
dotnet build .\EletricAutoCAD.csproj -c Release -p:AutoCADVersion=2026
```

Para instalações em outro diretório, informe, por exemplo, `-p:AutoCADInstallDir="D:\Autodesk\AutoCAD 2026"`.

Os builds são separados porque o AutoCAD 2023 usa .NET Framework 4.8 e o AutoCAD 2026 usa .NET 8. O mesmo código-fonte é compartilhado, mas cada versão deve carregar o DLL compilado para seu runtime e seus assemblies.

## Usar

1. Abra o ELETRIC e deixe a tela de cadastro de cômodos aberta. O aplicativo inicia a API somente em `127.0.0.1:47651`.
2. No AutoCAD correspondente à versão do DLL, execute `NETLOAD` e carregue `EletricAutoCAD.dll` da pasta `bin\Release\net48` (2023) ou `bin\Release\net8.0-windows` (2026). Para não repetir esse passo em cada sessão, adicione o DLL à Startup Suite em `APPLOAD`.
3. Execute `APPMEDIR`, escolha a unidade usada pelos valores das coordenadas e então o método de medição. Se desenhou 1 m como 1 unidade, escolha **Metros** mesmo que `INSUNITS` esteja configurado como milímetros. Se o quadrado de 1 m tem lados de 1000 unidades em um desenho em milímetros, escolha **Desenho**. O método **Pontos** é o padrão: clique sucessivamente nos vértices do contorno, contornando paredes, reentrâncias e recortes, e pressione Enter após pelo menos três pontos para fechar o contorno. Alternativamente, escolha **Polilinha** e selecione uma polilinha fechada.
4. No ELETRIC, escolha o tipo do cômodo, informe um nome se desejar e clique em **Adicionar**.

O método por pontos segue o fluxo de medição por pontos da ferramenta Área, fecha o contorno entre o último e o primeiro ponto e calcula a área e o perímetro desse polígono. Assim, não é necessário que as paredes já formem uma polilinha fechada. Durante a seleção, os pontos aparecem como círculos amarelos e os segmentos já percorridos em vermelho; o segmento de fechamento aparece em verde. A prévia é removida ao concluir ou cancelar. Clique em cada mudança de direção para representar o formato do cômodo; curvas e arcos são aproximados pelos pontos indicados. O cálculo considera o plano dos pontos e o fechamento do polígono, portanto desenhe os pontos na vista/plano do cômodo.

Para criar um botão na faixa de opções do AutoCAD, execute `CUI`, crie um comando com o nome desejado e a macro `^C^CAPPMEDIR`, depois arraste esse comando para um painel da faixa de opções e aplique as alterações.

O comando exibe o valor de `INSUNITS` e permite usar essa unidade ou informar explicitamente metros, centímetros, milímetros, pés ou polegadas. A opção **Desenho** também converte quilômetros, decímetros, decâmetros, hectômetros, pés topográficos dos EUA e jardas; unidades não reconhecidas são rejeitadas. A unidade escolhida deve corresponder aos números usados nas coordenadas, não apenas à unidade desejada para o resultado. O método Polilinha continua aceitando apenas polilinhas fechadas; outros objetos e contornos compostos ainda não são suportados.

O endpoint recebe `POST /api/autocad/measurement` com `application/x-www-form-urlencoded`, por exemplo `area=20.5&perimetro=18`. Ele escuta apenas no loopback e só fica ativo enquanto o ELETRIC estiver aberto.
