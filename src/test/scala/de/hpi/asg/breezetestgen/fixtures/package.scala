package de.hpi.asg.breezetestgen

import de.hpi.asg.breezetestgen.domain.Netlist
import de.uni_potsdam.hpi.asg.common.io.WorkingdirGenerator

package object fixtures {
  def breezeFilePath = "/breezefiles/gcd.breeze"
  def breezeFile: java.io.File = new java.io.File(getClass.getResource(breezeFilePath).getPath)
  def gcdNetlist(): Netlist = {
    WorkingdirGenerator.getInstance.create(null, null, "BrzTestGenTmp", null)
    try
      BreezeTransformer.parse(breezeFile).get
    finally
      WorkingdirGenerator.getInstance.delete()
  }
}
